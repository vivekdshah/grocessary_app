import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'my_printer_plugin_platform_interface.dart';

/// An implementation of [MyPrinterPluginPlatform] that uses method channels.
class MethodChannelMyPrinterPlugin extends MyPrinterPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('my_printer_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
