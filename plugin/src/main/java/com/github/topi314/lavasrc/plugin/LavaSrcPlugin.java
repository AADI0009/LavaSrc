package com.github.topi314.lavasrc.plugin;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@Service
public class LavaSrcPlugin implements AudioPlayerManagerConfiguration {

	private static final Logger log = LoggerFactory.getLogger(LavaSrcPlugin.class);

	private final LavaSrcConfig pluginConfig;
	private final SourcesConfig sourcesConfig;
	private final SpotifyConfig spotifyConfig;
	private final AppleMusicConfig appleMusicConfig;
	private final YandexMusicConfig yandexMusicConfig;

	private final DeezerConfig deezerConfig;

	public LavaSrcPlugin(LavaSrcConfig pluginConfig, SourcesConfig sourcesConfig, SpotifyConfig spotifyConfig, AppleMusicConfig appleMusicConfig, DeezerConfig deezerConfig, YandexMusicConfig yandexMusicConfig) {
		log.info("Loading LavaSrc plugin...");
		this.pluginConfig = pluginConfig;
		this.sourcesConfig = sourcesConfig;
		this.spotifyConfig = spotifyConfig;
		this.appleMusicConfig = appleMusicConfig;
		this.deezerConfig = deezerConfig;
		this.yandexMusicConfig = yandexMusicConfig;
	}

	@Override
	public AudioPlayerManager configure(AudioPlayerManager manager) {
		if (this.sourcesConfig.isSpotify()) {
			log.info("Registering Spotify audio source manager...");
			var spotifySourceManager = new SpotifySourceManager(this.pluginConfig.getProviders(), this.spotifyConfig.getClientId(), this.spotifyConfig.getClientSecret(), this.spotifyConfig.getCountryCode(), manager);
			if (this.spotifyConfig.getPlaylistLoadLimit() > 0) {
				spotifySourceManager.setPlaylistPageLimit(this.spotifyConfig.getPlaylistLoadLimit());
			}
			if (this.spotifyConfig.getAlbumLoadLimit() > 0) {
				spotifySourceManager.setAlbumPageLimit(this.spotifyConfig.getAlbumLoadLimit());
			}
			manager.registerSourceManager(spotifySourceManager);
		}
		if (this.sourcesConfig.isAppleMusic()) {
			log.info("Registering Apple Music audio source manager...");
			AppleMusicSourceManager appleMusicSourceManager;
			if (appleMusicConfig.getMusicKitKey() != null) {
				Objects.requireNonNull(appleMusicConfig.getTeamID(), () -> "teamID is required when using musicKitKey");
				Objects.requireNonNull(appleMusicConfig.getKeyID(), () -> "keyID is required when using musicKitKey");

				try {
					appleMusicSourceManager = AppleMusicSourceManager.fromMusicKitKey(appleMusicConfig.getMusicKitKey(), appleMusicConfig.getKeyID(), appleMusicConfig.getTeamID(),appleMusicConfig.getCountryCode(), manager, new DefaultMirroringAudioTrackResolver(pluginConfig.getProviders()));
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					throw new RuntimeException(e);
				}
			} else {
				appleMusicSourceManager = new AppleMusicSourceManager(this.pluginConfig.getProviders(), this.appleMusicConfig.getMediaAPIToken(), this.appleMusicConfig.getCountryCode(), manager);
			}

			if (this.appleMusicConfig.getPlaylistLoadLimit() > 0) {
				appleMusicSourceManager.setPlaylistPageLimit(this.appleMusicConfig.getPlaylistLoadLimit());
			}
			if (this.appleMusicConfig.getAlbumLoadLimit() > 0) {
				appleMusicSourceManager.setAlbumPageLimit(this.appleMusicConfig.getAlbumLoadLimit());
			}
			manager.registerSourceManager(appleMusicSourceManager);
		}
		if (this.sourcesConfig.isDeezer()) {
			log.info("Registering Deezer audio source manager...");
			manager.registerSourceManager(new DeezerAudioSourceManager(this.deezerConfig.getMasterDecryptionKey()));
		}
		if (this.sourcesConfig.isYandexMusic()) {
			log.info("Registering Yandex Music audio source manager...");
			manager.registerSourceManager(new YandexMusicSourceManager(this.yandexMusicConfig.getAccessToken()));
		}
		return manager;
	}

}