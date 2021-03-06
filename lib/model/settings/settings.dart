import 'package:aves/model/settings/coordinate_format.dart';
import 'package:aves/model/settings/home_page.dart';
import 'package:aves/widgets/fullscreen/info/location_section.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../source/enums.dart';

final Settings settings = Settings._private();

typedef SettingsCallback = void Function(String key, dynamic oldValue, dynamic newValue);

class Settings extends ChangeNotifier {
  static SharedPreferences _prefs;

  Settings._private();

  // app
  static const hasAcceptedTermsKey = 'has_accepted_terms';
  static const mustBackTwiceToExitKey = 'must_back_twice_to_exit';
  static const homePageKey = 'home_page';
  static const catalogTimeZoneKey = 'catalog_time_zone';

  // collection
  static const collectionGroupFactorKey = 'collection_group_factor';
  static const collectionSortFactorKey = 'collection_sort_factor';
  static const collectionTileExtentKey = 'collection_tile_extent';

  // filter grids
  static const albumSortFactorKey = 'album_sort_factor';

  // info
  static const infoMapStyleKey = 'info_map_style';
  static const infoMapZoomKey = 'info_map_zoom';
  static const coordinateFormatKey = 'coordinates_format';

  // rendering
  static const svgBackgroundKey = 'svg_background';

  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  Future<void> reset() {
    return _prefs.clear();
  }

  // app

  bool get hasAcceptedTerms => getBoolOrDefault(hasAcceptedTermsKey, false);

  set hasAcceptedTerms(bool newValue) => setAndNotify(hasAcceptedTermsKey, newValue);

  bool get mustBackTwiceToExit => getBoolOrDefault(mustBackTwiceToExitKey, true);

  set mustBackTwiceToExit(bool newValue) => setAndNotify(mustBackTwiceToExitKey, newValue);

  HomePageSetting get homePage => getEnumOrDefault(homePageKey, HomePageSetting.collection, HomePageSetting.values);

  set homePage(HomePageSetting newValue) => setAndNotify(homePageKey, newValue.toString());

  String get catalogTimeZone => _prefs.getString(catalogTimeZoneKey) ?? '';

  set catalogTimeZone(String newValue) => setAndNotify(catalogTimeZoneKey, newValue);

  // collection

  EntryGroupFactor get collectionGroupFactor => getEnumOrDefault(collectionGroupFactorKey, EntryGroupFactor.month, EntryGroupFactor.values);

  set collectionGroupFactor(EntryGroupFactor newValue) => setAndNotify(collectionGroupFactorKey, newValue.toString());

  EntrySortFactor get collectionSortFactor => getEnumOrDefault(collectionSortFactorKey, EntrySortFactor.date, EntrySortFactor.values);

  set collectionSortFactor(EntrySortFactor newValue) => setAndNotify(collectionSortFactorKey, newValue.toString());

  double get collectionTileExtent => _prefs.getDouble(collectionTileExtentKey) ?? 0;

  set collectionTileExtent(double newValue) => setAndNotify(collectionTileExtentKey, newValue);

  // filter grids

  ChipSortFactor get albumSortFactor => getEnumOrDefault(albumSortFactorKey, ChipSortFactor.name, ChipSortFactor.values);

  set albumSortFactor(ChipSortFactor newValue) => setAndNotify(albumSortFactorKey, newValue.toString());

  // info

  EntryMapStyle get infoMapStyle => getEnumOrDefault(infoMapStyleKey, EntryMapStyle.stamenWatercolor, EntryMapStyle.values);

  set infoMapStyle(EntryMapStyle newValue) => setAndNotify(infoMapStyleKey, newValue.toString());

  double get infoMapZoom => _prefs.getDouble(infoMapZoomKey) ?? 12;

  set infoMapZoom(double newValue) => setAndNotify(infoMapZoomKey, newValue);

  CoordinateFormat get coordinateFormat => getEnumOrDefault(coordinateFormatKey, CoordinateFormat.dms, CoordinateFormat.values);

  set coordinateFormat(CoordinateFormat newValue) => setAndNotify(coordinateFormatKey, newValue.toString());

  // rendering

  int get svgBackground => _prefs.getInt(svgBackgroundKey) ?? 0xFFFFFFFF;

  set svgBackground(int newValue) => setAndNotify(svgBackgroundKey, newValue);

  // utils

  // `RoutePredicate`
  RoutePredicate navRemoveRoutePredicate(String pushedRouteName) {
    final home = homePage.routeName;
    return (route) => pushedRouteName != home && route.settings?.name == home;
  }

  // convenience methods

  // ignore: avoid_positional_boolean_parameters
  bool getBoolOrDefault(String key, bool defaultValue) => _prefs.getKeys().contains(key) ? _prefs.getBool(key) : defaultValue;

  T getEnumOrDefault<T>(String key, T defaultValue, Iterable<T> values) {
    final valueString = _prefs.getString(key);
    for (final element in values) {
      if (element.toString() == valueString) {
        return element;
      }
    }
    return defaultValue;
  }

  List<T> getEnumListOrDefault<T>(String key, List<T> defaultValue, Iterable<T> values) {
    return _prefs.getStringList(key)?.map((s) => values.firstWhere((el) => el.toString() == s, orElse: () => null))?.where((el) => el != null)?.toList() ?? defaultValue;
  }

  void setAndNotify(String key, dynamic newValue) {
    var oldValue = _prefs.get(key);
    if (newValue == null) {
      _prefs.remove(key);
    } else if (newValue is String) {
      oldValue = _prefs.getString(key);
      _prefs.setString(key, newValue);
    } else if (newValue is List<String>) {
      oldValue = _prefs.getStringList(key);
      _prefs.setStringList(key, newValue);
    } else if (newValue is int) {
      oldValue = _prefs.getInt(key);
      _prefs.setInt(key, newValue);
    } else if (newValue is double) {
      oldValue = _prefs.getDouble(key);
      _prefs.setDouble(key, newValue);
    } else if (newValue is bool) {
      oldValue = _prefs.getBool(key);
      _prefs.setBool(key, newValue);
    }
    if (oldValue != newValue) {
      notifyListeners();
    }
  }
}
