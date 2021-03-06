import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';

class Constants {
  // as of Flutter v1.11.0, overflowing `Text` miscalculates height and some text (e.g. 'Å') is clipped
  // so we give it a `strutStyle` with a slightly larger height
  static const overflowStrutStyle = StrutStyle(height: 1.3);

  static const titleTextStyle = TextStyle(
    color: Color(0xFFEEEEEE),
    fontSize: 20,
    fontFamily: 'Concourse Caps',
  );

  static const List<Dependency> androidDependencies = [
    Dependency(
      name: 'CWAC-Document',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/commonsguy/cwac-document/blob/master/LICENSE',
      sourceUrl: 'https://github.com/commonsguy/cwac-document',
    ),
    Dependency(
      name: 'Glide',
      license: 'Apache 2.0, BSD 2-Clause',
      licenseUrl: 'https://github.com/bumptech/glide/blob/master/LICENSE',
      sourceUrl: 'https://github.com/bumptech/glide',
    ),
    Dependency(
      name: 'Guava',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/google/guava/blob/master/COPYING',
      sourceUrl: 'https://github.com/google/guava',
    ),
    Dependency(
      name: 'Metadata Extractor',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/drewnoakes/metadata-extractor/blob/master/LICENSE',
      sourceUrl: 'https://github.com/drewnoakes/metadata-extractor',
    ),
  ];

  static const List<Dependency> flutterPackages = [
    Dependency(
      name: 'Flutter',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/flutter/flutter/blob/master/LICENSE',
      sourceUrl: 'https://github.com/flutter/flutter',
    ),
    Dependency(
      name: 'Charts',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/google/charts/blob/master/LICENSE',
      sourceUrl: 'https://github.com/google/charts',
    ),
    Dependency(
      name: 'Collection',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/dart-lang/collection/blob/master/LICENSE',
      sourceUrl: 'https://github.com/dart-lang/collection',
    ),
    Dependency(
      name: 'Draggable Scrollbar',
      license: 'MIT',
      licenseUrl: 'https://github.com/fluttercommunity/flutter-draggable-scrollbar/blob/master/LICENSE',
      sourceUrl: 'https://github.com/fluttercommunity/flutter-draggable-scrollbar',
    ),
    Dependency(
      name: 'Event Bus',
      license: 'MIT',
      licenseUrl: 'https://github.com/marcojakob/dart-event-bus/blob/master/LICENSE',
      sourceUrl: 'https://github.com/marcojakob/dart-event-bus',
    ),
    Dependency(
      name: 'Expansion Tile Card',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/Skylled/expansion_tile_card/blob/master/LICENSE',
      sourceUrl: 'https://github.com/Skylled/expansion_tile_card',
    ),
    Dependency(
      name: 'Firebase Crashlytics',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/FirebaseExtended/flutterfire/blob/master/packages/firebase_crashlytics/LICENSE',
      sourceUrl: 'https://github.com/FirebaseExtended/flutterfire/tree/master/packages/firebase_crashlytics',
    ),
    Dependency(
      name: 'Flushbar',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/AndreHaueisen/flushbar/blob/master/LICENSE',
      sourceUrl: 'https://github.com/AndreHaueisen/flushbar',
    ),
    Dependency(
      name: 'Flutter ijkplayer',
      license: 'MIT',
      licenseUrl: 'https://github.com/CaiJingLong/flutter_ijkplayer/blob/master/LICENSE',
      sourceUrl: 'https://github.com/CaiJingLong/flutter_ijkplayer',
    ),
    Dependency(
      name: 'Flutter Markdown',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/flutter/flutter_markdown/blob/master/LICENSE',
      sourceUrl: 'https://github.com/flutter/flutter_markdown',
    ),
    Dependency(
      name: 'Flutter Native Timezone',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/pinkfish/flutter_native_timezone/blob/master/LICENSE',
      sourceUrl: 'https://github.com/pinkfish/flutter_native_timezone',
    ),
    Dependency(
      name: 'Flutter Staggered Animations',
      license: 'MIT',
      licenseUrl: 'https://github.com/mobiten/flutter_staggered_animations/blob/master/LICENSE',
      sourceUrl: 'https://github.com/mobiten/flutter_staggered_animations',
    ),
    Dependency(
      name: 'Flutter SVG',
      license: 'MIT',
      licenseUrl: 'https://github.com/dnfield/flutter_svg/blob/master/LICENSE',
      sourceUrl: 'https://github.com/dnfield/flutter_svg',
    ),
    Dependency(
      name: 'Geocoder',
      license: 'MIT',
      licenseUrl: 'https://github.com/aloisdeniel/flutter_geocoder/blob/master/LICENSE',
      sourceUrl: 'https://github.com/aloisdeniel/flutter_geocoder',
    ),
    Dependency(
      name: 'Google Maps for Flutter',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/flutter/plugins/blob/master/packages/google_maps_flutter/google_maps_flutter/LICENSE',
      sourceUrl: 'https://github.com/flutter/plugins/blob/master/packages/google_maps_flutter/google_maps_flutter',
    ),
    Dependency(
      name: 'Intl',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/dart-lang/intl/blob/master/LICENSE',
      sourceUrl: 'https://github.com/dart-lang/intl',
    ),
    Dependency(
      name: 'Outline Material Icons',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/lucaslcode/outline_material_icons/blob/master/LICENSE',
      sourceUrl: 'https://github.com/lucaslcode/outline_material_icons',
    ),
    Dependency(
      name: 'Palette Generator',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/flutter/packages/blob/master/packages/palette_generator/LICENSE',
      sourceUrl: 'https://github.com/flutter/packages/tree/master/packages/palette_generator',
    ),
    Dependency(
      name: 'PDF for Dart and Flutter',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/DavBfr/dart_pdf/blob/master/LICENSE',
      sourceUrl: 'https://github.com/DavBfr/dart_pdf',
    ),
    Dependency(
      name: 'Pedantic',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/dart-lang/pedantic/blob/master/LICENSE',
      sourceUrl: 'https://github.com/dart-lang/pedantic',
    ),
    Dependency(
      name: 'Percent Indicator',
      license: 'BSD 2-Clause',
      licenseUrl: 'https://github.com/diegoveloper/flutter_percent_indicator/blob/master/LICENSE',
      sourceUrl: 'https://github.com/diegoveloper/flutter_percent_indicator/',
    ),
    Dependency(
      name: 'Permission Handler',
      license: 'MIT',
      licenseUrl: 'https://github.com/Baseflow/flutter-permission-handler/blob/develop/permission_handler/LICENSE',
      sourceUrl: 'https://github.com/Baseflow/flutter-permission-handler',
    ),
    Dependency(
      name: 'Photo View',
      license: 'MIT',
      licenseUrl: 'https://github.com/renancaraujo/photo_view/blob/master/LICENSE',
      sourceUrl: 'https://github.com/renancaraujo/photo_view',
    ),
    Dependency(
      name: 'Printing',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/DavBfr/dart_pdf/blob/master/LICENSE',
      sourceUrl: 'https://github.com/DavBfr/dart_pdf',
    ),
    Dependency(
      name: 'Provider',
      license: 'MIT',
      licenseUrl: 'https://github.com/rrousselGit/provider/blob/master/LICENSE',
      sourceUrl: 'https://github.com/rrousselGit/provider',
    ),
    Dependency(
      name: 'Screen',
      license: 'MIT',
      licenseUrl: 'https://github.com/clovisnicolas/flutter_screen/blob/master/LICENSE',
      sourceUrl: 'https://github.com/clovisnicolas/flutter_screen',
    ),
    Dependency(
      name: 'Shared Preferences',
      license: 'BSD 3-Clause',
      licenseUrl: 'https://github.com/flutter/plugins/blob/master/packages/shared_preferences/shared_preferences/LICENSE',
      sourceUrl: 'https://github.com/flutter/plugins/tree/master/packages/shared_preferences/shared_preferences',
    ),
    Dependency(
      name: 'sqflite',
      license: 'MIT',
      licenseUrl: 'https://github.com/tekartik/sqflite/blob/master/sqflite/LICENSE',
      sourceUrl: 'https://github.com/tekartik/sqflite',
    ),
    Dependency(
      name: 'Streams Channel',
      license: 'Apache 2.0',
      licenseUrl: 'https://github.com/loup-v/streams_channel/blob/master/LICENSE',
      sourceUrl: 'https://github.com/loup-v/streams_channel',
    ),
    Dependency(
      name: 'Tuple',
      license: 'BSD 2-Clause',
      licenseUrl: 'https://github.com/dart-lang/tuple/blob/master/LICENSE',
      sourceUrl: 'https://github.com/dart-lang/tuple',
    ),
    Dependency(
      name: 'UUID',
      license: 'MIT',
      licenseUrl: 'https://github.com/Daegalus/dart-uuid/blob/master/LICENSE',
      sourceUrl: 'https://github.com/Daegalus/dart-uuid',
    ),
  ];
}

class Dependency {
  final String name;
  final String license;
  final String sourceUrl;
  final String licenseUrl;

  const Dependency({
    @required this.name,
    @required this.license,
    @required this.licenseUrl,
    @required this.sourceUrl,
  });
}
