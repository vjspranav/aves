import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui show Codec;

import 'package:aves/services/image_file_service.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:pedantic/pedantic.dart';

class UriImage extends ImageProvider<UriImage> {
  const UriImage({
    @required this.uri,
    @required this.mimeType,
    @required this.orientationDegrees,
    this.expectedContentLength,
    this.scale = 1.0,
  })  : assert(uri != null),
        assert(scale != null);

  final String uri, mimeType;
  final int orientationDegrees, expectedContentLength;
  final double scale;

  @override
  Future<UriImage> obtainKey(ImageConfiguration configuration) {
    return SynchronousFuture<UriImage>(this);
  }

  @override
  ImageStreamCompleter load(UriImage key, DecoderCallback decode) {
    final chunkEvents = StreamController<ImageChunkEvent>();

    return MultiFrameImageStreamCompleter(
      codec: _loadAsync(key, decode, chunkEvents),
      scale: key.scale,
      chunkEvents: chunkEvents.stream,
      informationCollector: () sync* {
        yield ErrorDescription('uri=$uri, mimeType=$mimeType');
      },
    );
  }

  Future<ui.Codec> _loadAsync(UriImage key, DecoderCallback decode, StreamController<ImageChunkEvent> chunkEvents) async {
    assert(key == this);

    try {
      final bytes = await ImageFileService.getImage(
        uri,
        mimeType,
        orientationDegrees: orientationDegrees,
        expectedContentLength: expectedContentLength,
        onBytesReceived: (cumulative, total) {
          chunkEvents.add(ImageChunkEvent(
            cumulativeBytesLoaded: cumulative,
            expectedTotalBytes: total,
          ));
        },
      );
      return await decode(bytes ?? Uint8List(0));
    } finally {
      unawaited(chunkEvents.close());
    }
  }

  @override
  bool operator ==(Object other) {
    if (other.runtimeType != runtimeType) return false;
    return other is UriImage && other.uri == uri && other.mimeType == mimeType && other.scale == scale;
  }

  @override
  int get hashCode => hashValues(uri, scale);

  @override
  String toString() => '${objectRuntimeType(this, 'UriImage')}(uri=$uri, mimeType=$mimeType, scale=$scale)';
}
