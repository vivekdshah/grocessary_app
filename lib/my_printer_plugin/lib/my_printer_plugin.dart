
import 'package:flutter/services.dart';
import 'package:get/get_connect/http/src/response/response.dart';

import 'my_printer_plugin_platform_interface.dart';

class MyPrinterPlugin {
  static const MethodChannel platform = MethodChannel('samples.flutter.dev/MyChannel');

  Future<String?> getPlatformVersion() {
    return MyPrinterPluginPlatform.instance.getPlatformVersion();
  }

   // static Future<void> printReceipt() async {
   //   try {
   //     await _channel.invokeMethod<void>('print_receipt');
   //   } catch (e) {
   //     print('Error invoking print_receipt: $e');
   //   }
   // }

 static Future<void> startPrinting(Response apiResponse) async {
    if (apiResponse.statusCode == 200) {
      platform.invokeMethod('print_receipt', {'data': apiResponse.body});
    }
  }
}
