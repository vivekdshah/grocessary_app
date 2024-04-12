import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'my_printer_plugin_method_channel.dart';

abstract class MyPrinterPluginPlatform extends PlatformInterface {
  /// Constructs a MyPrinterPluginPlatform.
  MyPrinterPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static MyPrinterPluginPlatform _instance = MethodChannelMyPrinterPlugin();

  /// The default instance of [MyPrinterPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelMyPrinterPlugin].
  static MyPrinterPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MyPrinterPluginPlatform] when
  /// they register themselves.
  static set instance(MyPrinterPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
