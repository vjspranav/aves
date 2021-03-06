import 'package:aves/model/image_entry.dart';
import 'package:aves/model/source/collection_source.dart';
import 'package:aves/utils/android_file_utils.dart';
import 'package:collection/collection.dart';
import 'package:path/path.dart';

mixin AlbumMixin on SourceBase {
  final Set<String> _folderPaths = {};

  List<String> sortedAlbums = List.unmodifiable([]);

  void updateAlbums() {
    final sorted = _folderPaths.toList()
      ..sort((a, b) {
        final ua = getUniqueAlbumName(a);
        final ub = getUniqueAlbumName(b);
        return compareAsciiUpperCase(ua, ub);
      });
    sortedAlbums = List.unmodifiable(sorted);
    invalidateFilterEntryCounts();
    eventBus.fire(AlbumsChangedEvent());
  }

  String getUniqueAlbumName(String album) {
    final volumeRoot = androidFileUtils.getStorageVolume(album)?.path ?? '';
    final otherAlbums = _folderPaths.where((item) => item != album && item.startsWith(volumeRoot));
    final parts = album.split(separator);
    var partCount = 0;
    String testName;
    do {
      testName = separator + parts.skip(parts.length - ++partCount).join(separator);
    } while (otherAlbums.any((item) => item.endsWith(testName)));
    return parts.skip(parts.length - partCount).join(separator);
  }

  Map<String, ImageEntry> getAlbumEntries() {
    final entries = sortedEntriesForFilterList;
    final regularAlbums = <String>[], appAlbums = <String>[], specialAlbums = <String>[];
    for (var album in sortedAlbums) {
      switch (androidFileUtils.getAlbumType(album)) {
        case AlbumType.regular:
          regularAlbums.add(album);
          break;
        case AlbumType.app:
          appAlbums.add(album);
          break;
        default:
          specialAlbums.add(album);
          break;
      }
    }
    return Map.fromEntries([...specialAlbums, ...appAlbums, ...regularAlbums].map((album) => MapEntry(
          album,
          entries.firstWhere((entry) => entry.directory == album, orElse: () => null),
        )));
  }

  void addFolderPath(Iterable<String> albums) => _folderPaths.addAll(albums);

  void cleanEmptyAlbums([Set<String> albums]) {
    final emptyAlbums = (albums ?? _folderPaths).where(_isEmptyAlbum).toList();
    if (emptyAlbums.isNotEmpty) {
      _folderPaths.removeAll(emptyAlbums);
      updateAlbums();
    }
  }

  bool _isEmptyAlbum(String album) => !rawEntries.any((entry) => entry.directory == album);
}

class AlbumsChangedEvent {}
