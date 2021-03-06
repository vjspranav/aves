import 'package:aves/model/settings/coordinate_format.dart';
import 'package:aves/model/settings/home_page.dart';
import 'package:aves/model/settings/settings.dart';
import 'package:aves/widgets/common/aves_selection_dialog.dart';
import 'package:aves/widgets/common/data_providers/media_query_data_provider.dart';
import 'package:aves/widgets/common/highlight_title.dart';
import 'package:aves/widgets/settings/svg_background.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class SettingsPage extends StatelessWidget {
  static const routeName = '/settings';

  @override
  Widget build(BuildContext context) {
    return MediaQueryDataProvider(
      child: DefaultTabController(
        length: 4,
        child: Scaffold(
          appBar: AppBar(
            title: Text('Settings'),
          ),
          body: SafeArea(
            child: Consumer<Settings>(
              builder: (context, settings, child) => ListView(
                padding: EdgeInsets.symmetric(vertical: 16),
                children: [
                  SectionTitle('Navigation'),
                  ListTile(
                    title: Text('Home'),
                    subtitle: Text(settings.homePage.name),
                    onTap: () async {
                      final value = await showDialog<HomePageSetting>(
                        context: context,
                        builder: (context) => AvesSelectionDialog<HomePageSetting>(
                          initialValue: settings.homePage,
                          options: Map.fromEntries(HomePageSetting.values.map((v) => MapEntry(v, v.name))),
                          title: 'Home',
                        ),
                      );
                      if (value != null) {
                        settings.homePage = value;
                      }
                    },
                  ),
                  SwitchListTile(
                    value: settings.mustBackTwiceToExit,
                    onChanged: (v) => settings.mustBackTwiceToExit = v,
                    title: Text('Tap “back” twice to exit'),
                  ),
                  SectionTitle('Display'),
                  ListTile(
                    title: Text('SVG background'),
                    trailing: SvgBackgroundSelector(),
                  ),
                  ListTile(
                    title: Text('Coordinate format'),
                    subtitle: Text(settings.coordinateFormat.name),
                    onTap: () async {
                      final value = await showDialog<CoordinateFormat>(
                        context: context,
                        builder: (context) => AvesSelectionDialog<CoordinateFormat>(
                          initialValue: settings.coordinateFormat,
                          options: Map.fromEntries(CoordinateFormat.values.map((v) => MapEntry(v, v.name))),
                          title: 'Coordinate Format',
                        ),
                      );
                      if (value != null) {
                        settings.coordinateFormat = value;
                      }
                    },
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class SectionTitle extends StatelessWidget {
  final String text;

  const SectionTitle(this.text);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(left: 16, top: 6, right: 16, bottom: 12),
      child: HighlightTitle(text),
    );
  }
}
