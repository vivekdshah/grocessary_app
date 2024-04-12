import 'package:flutter_test/flutter_test.dart';
import 'package:my_printer_plugin/my_printer_plugin.dart';
import 'package:my_printer_plugin/my_printer_plugin_platform_interface.dart';
import 'package:my_printer_plugin/my_printer_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMyPrinterPluginPlatform
    with MockPlatformInterfaceMixin
    implements MyPrinterPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MyPrinterPluginPlatform initialPlatform = MyPrinterPluginPlatform.instance;

  test('$MethodChannelMyPrinterPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMyPrinterPlugin>());
  });

  test('getPlatformVersion', () async {
    MyPrinterPlugin myPrinterPlugin = MyPrinterPlugin();
    MockMyPrinterPluginPlatform fakePlatform = MockMyPrinterPluginPlatform();
    MyPrinterPluginPlatform.instance = fakePlatform;

    expect(await myPrinterPlugin.getPlatformVersion(), '42');
  });
}
