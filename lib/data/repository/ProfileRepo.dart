
import 'package:get/get_connect/http/src/response/response.dart';

import '../../util/app_constants.dart';
import '../api/api_client.dart';

class ProfileRepo {
  ApiClient apiClient;
  ProfileRepo({required this.apiClient});

  Future<Response> getAddAccountDetails() async {
    return await apiClient.getData(AppConstants.addAccountDetails);
  }
}