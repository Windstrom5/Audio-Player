# Vendored: JustFLAC (org.kc7bfi.jflac)

This package is vendored source from drogatkin/JustFLAC
(https://github.com/drogatkin/JustFLAC), a fork of the original jFLAC
(http://jflac.sourceforge.net/) that fixes several decode bugs present in
the more commonly-packaged `org.jflac:jflac-codec` Maven fork
(nguillaumin/jflac), most importantly:

- 24-bit (and wider) sample decoding, which the upstream fork's README and
  an independent bug report both confirm is broken.
- `FLACDecoder`'s internal `channels` field, which the Maven fork never
  assigns (silently stays 0), making its own `decodeFrame()` produce empty
  PCM for every frame at every bit depth when called via the public
  readNextFrame()/decodeFrame() API path (as opposed to its internal
  decode() loop). This fork assigns it correctly in readFrame().

## Why vendored instead of a dependency

JustFLAC has no confirmed reliable Maven Central or JitPack distribution —
other projects that depend on it (e.g. FLACtagger) resort to vendoring the
source directly for the same reason. Vendoring here means the FLAC
fallback decoder compiles as an ordinary part of this app module, with no
external build-service dependency (JitPack building an arbitrary
unreleased GitHub tree on demand) that could fail independently of this
project's own build.

## What's included vs. excluded

Included: the core decoder (`FLACDecoder`, `ChannelData`, `Constants`,
predictors, `frame/`, `io/`, `metadata/`, and the `util/` classes the
decoder itself depends on).

Excluded (deleted from the original tree):
- `FLACEncoder.java` — encoding isn't used by this app.
- `apps/` — the upstream example CLI apps.
- `sound/spi/` — Java Sound SPI integration, which depends on
  `javax.sound.sampled`, a desktop-only API unavailable on Android.

## License

JustFLAC is licensed under the GNU LGPL (see the license header in each
source file), consistent with the original jFLAC/libFLAC lineage. If this
app is distributed, the LGPL's source-availability and dynamic-linking
requirements apply to this package.

## Usage in this app

See `com.thesis.bitperfectusb.playback.FlacDecoder` — specifically the
private `JflacPcmSource` class — for how this is used as a last-resort
software fallback when every MediaCodec FLAC decoder candidate fails on
device.
