
import 'package:efood_kitchen/data/repository/ProfileRepo.dart';
import 'package:get/get_connect/http/src/response/response.dart';
import 'package:get/get_state_manager/src/rx_flutter/rx_disposable.dart';
import 'package:get/get_state_manager/src/simple/get_controllers.dart';

import '../data/model/response/add_account_details_model.dart';

class ProfileController extends GetxController implements GetxService {
  final ProfileRepo profileRepo;
  ProfileController({required this.profileRepo}) ;
  bool _isLoading = false;
  bool get isLoading => _isLoading;
  AddAccountDetailsModel _addAccountDetailsModel = AddAccountDetailsModel();
  AddAccountDetailsModel get addAccountDetailsModel => _addAccountDetailsModel;

  Future<Response> getAddAccountDetails() async {
    _isLoading = true;
    update();
    Response response = await profileRepo.getAddAccountDetails();
    if (response.statusCode == 200) {
      _addAccountDetailsModel =  AddAccountDetailsModel.fromJson(response.body);
    }
    _isLoading = false;
    update();
    return response;
  }
}
