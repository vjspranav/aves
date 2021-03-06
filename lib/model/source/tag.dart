import 'package:aves/model/image_entry.dart';
import 'package:aves/model/image_metadata.dart';
import 'package:aves/model/metadata_db.dart';
import 'package:aves/model/source/collection_source.dart';
import 'package:collection/collection.dart';
import 'package:flutter/foundation.dart';

mixin TagMixin on SourceBase {
  static const _commitCountThreshold = 300;

  List<String> sortedTags = List.unmodifiable([]);

  Future<void> loadCatalogMetadata() async {
    final stopwatch = Stopwatch()..start();
    final saved = await metadataDb.loadMetadataEntries();
    rawEntries.forEach((entry) {
      final contentId = entry.contentId;
      entry.catalogMetadata = saved.firstWhere((metadata) => metadata.contentId == contentId, orElse: () => null);
    });
    debugPrint('$runtimeType loadCatalogMetadata complete in ${stopwatch.elapsed.inMilliseconds}ms for ${saved.length} entries');
    onCatalogMetadataChanged();
  }

  Future<void> catalogEntries() async {
//    final stopwatch = Stopwatch()..start();
    final todo = rawEntries.where((entry) => !entry.isCatalogued && !entry.isSvg).toList();
    if (todo.isEmpty) return;

    var progressDone = 0;
    final progressTotal = todo.length;
    setProgress(done: progressDone, total: progressTotal);

    final newMetadata = <CatalogMetadata>[];
    await Future.forEach<ImageEntry>(todo, (entry) async {
      await entry.catalog(background: true);
      if (entry.isCatalogued) {
        newMetadata.add(entry.catalogMetadata);
        if (newMetadata.length >= _commitCountThreshold) {
          await metadataDb.saveMetadata(List.unmodifiable(newMetadata));
          onCatalogMetadataChanged();
          newMetadata.clear();
        }
      }
      setProgress(done: ++progressDone, total: progressTotal);
    });
    await metadataDb.saveMetadata(List.unmodifiable(newMetadata));
    onCatalogMetadataChanged();
//    debugPrint('$runtimeType catalogEntries complete in ${stopwatch.elapsed.inSeconds}s');
  }

  void onCatalogMetadataChanged() {
    updateTags();
    eventBus.fire(CatalogMetadataChangedEvent());
  }

  void updateTags() {
    final tags = rawEntries.expand((entry) => entry.xmpSubjects).toSet().toList()..sort(compareAsciiUpperCase);
    sortedTags = List.unmodifiable(tags);
    invalidateFilterEntryCounts();
    eventBus.fire(TagsChangedEvent());
  }

  Map<String, ImageEntry> getTagEntries() {
    final entries = sortedEntriesForFilterList;
    return Map.fromEntries(sortedTags.map((tag) => MapEntry(
          tag,
          entries.firstWhere((entry) => entry.xmpSubjects.contains(tag), orElse: () => null),
        )));
  }
}

class CatalogMetadataChangedEvent {}

class TagsChangedEvent {}
