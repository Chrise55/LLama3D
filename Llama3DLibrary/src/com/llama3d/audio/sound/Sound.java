package com.llama3d.audio.sound;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.AudioEffect;

public class Sound {

	// ===================================================================
	// Private Static Fields
	// ===================================================================

	private Random random = new Random(System.currentTimeMillis());

	// ===================================================================
	// Fields
	// ===================================================================

	protected AudioTrack track;
	protected AudioEffect sfx;
	private short[] buffer = new short[1024 * 128];

	// ===================================================================
	// Constructors
	// ===================================================================

	public Sound() {
		// ======== Create New AudioTrack ========
		int minSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT) * 128;
		this.track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_DEFAULT, minSize, AudioTrack.MODE_STATIC);
		this.track.setLoopPoints(0, this.buffer.length / 2 - 1, -1);
	}

	// ===================================================================
	// Public Methods
	// ===================================================================

	public void mix(int curveType1, int frequency1, int curveType2, int frequency2, int curveMixType, float curveMixFactor, float start, float end) {

		// ======== Create Temporarybuffer ========
		float[] tempBuffer = new float[this.buffer.length];
		int bufferLength = tempBuffer.length;
		curveMixFactor = (float) (Math.max(Math.min(curveMixFactor, 1.0), 0.0));
		// ======== Init Calculationvalues ========
		float t1 = 0, t2 = 0, t3 = 0;
		float[] v = new float[2];
		int[] ct = new int[] { curveType1, curveType2 };
		// ======== Calculate Temporary Values ========
		float[] inc = new float[] { (float) (Math.PI * 2.0) * (frequency1 / 44100.0f), (float) (Math.PI * 2.0) * (frequency2 / 44100.0f) };
		// ======= Edit Samples ========
		for (int i = 0; i < bufferLength; i++) {
			// ======== Calculate Temporary Values ========
			t1 = (float) i / (float) bufferLength;
			for (int u = 0; u < 2; u++) {
				// ======== Generate SoundCurves ========
				switch (ct[u]) {
				case Curve.SINE:
					v[u] = (float) Math.sin(i * inc[u]);
					break;
				case Curve.RECT:
					if (Math.sin(i * inc[u]) < 0.0f) {
						v[u] = +1.0f;
					} else {
						v[u] = -1.0f;
					}
					break;
				case Curve.UNSIGNED_RECT:
					if (Math.sin(i * inc[u]) < 0.0f) {
						v[u] = +1.0f;
					} else {
						v[u] = 0.0f;
					}
					break;
				case Curve.RANDOM:
					v[u] = random.nextFloat() * 2.0f - 1.0f;
					break;
				case Curve.LINEAR:
					v[u] = 1.0f;
					break;
				case Curve.HYPERBEL:
					if (t1 >= start && t1 <= end) {
						v[u] = 1.0f / (10.0f * (t1 - start) / (end - start));
					} else {
						v[u] = 0.0f;
					}
					break;
				default:
					v[u] = 0.0f;
					break;
				}
			}
			tempBuffer[i] = (float) Math.max(-1.0, Math.min(1.0, v[0] * v[1]));
		}

		// ======== Mix SoundCurves ========
		for (int i = 0; i < bufferLength; i++) {
			// ======== Calculate Temporary Values ========
			t1 = (float) i / (float) bufferLength;
			t2 = (float) 1.0 - curveMixFactor;
			t3 = (float) curveMixFactor;
			if (t1 >= start && t1 <= end) {
				// ======== Calculate Mix Factors ========
				switch (curveMixType) {
				case Curve.MULTIPLY:
					this.buffer[i] = (short) (tempBuffer[i] * curveMixFactor * this.buffer[i]);
					break;
				case Curve.REPLACE:
					this.buffer[i] = (short) (tempBuffer[i] * curveMixFactor * Short.MAX_VALUE);
					break;
				case Curve.FADE:
					this.buffer[i] = (short) (t2 * this.buffer[i] + t3 * tempBuffer[i] * Short.MAX_VALUE);
					break;
				}
			}
		}
	}

	public void play() {
		// ======== If Sound Exists Then Play ========
		if (this.track != null) {
			this.track.play();
		}
	}

	public void stop() {
		// ======== If Sound Exists Then Play ========
		if (this.track != null) {
			this.track.stop();
		}
	}

	public void releaseTrack() {
		// ======== If Sound Exists Then Release ========
		if (this.track != null) {
			this.track.release();
		}
	}

	public void writeSamples() {
		// ======== If Sound Exists Then Release ========
		if (this.track != null) {
			this.track.write(this.buffer, 0, this.buffer.length);
		}
	}

}
