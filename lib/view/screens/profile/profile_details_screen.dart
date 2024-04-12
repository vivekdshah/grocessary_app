import 'package:efood_kitchen/controller/profile_controller.dart';
import 'package:efood_kitchen/view/base/confirmation_dialog.dart';
import 'package:flutter/material.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:get/get.dart';

import '../../../controller/auth_controller.dart';
import '../../../controller/splash_controller.dart';
import '../../../util/dimensions.dart';
import '../../../util/images.dart';
import '../../../util/styles.dart';
import '../../base/custom_image.dart';
import 'YourWebViewScreen.dart';

class ProfileDetailsScreen extends StatefulWidget {
  const ProfileDetailsScreen({Key? key}) : super(key: key);

  @override
  State<ProfileDetailsScreen> createState() => _ProfileDetailsScreenState();
}

class _ProfileDetailsScreenState extends State<ProfileDetailsScreen> {
  late MyInAppBrowser browser;

  @override
  void initState() {
    super.initState();
  }

  void _loadStripe(String url) async {
    browser = MyInAppBrowser(
      context,
      onCloseMessage: (response) {
       String message = response ? 'Account linked successfully.' : 'Account linking failed.';
        showDialog(context: context,
            barrierDismissible: false,
            builder: (context) => ConfirmationDialog(
                icon: Images.logo,
                title: 'Account Status',
                description: message,
                yesButtonText: 'Okay',
                onYesPressed: (){
                  Get.back();
                }));
        debugPrint('Add account status: $response');
      },
    );
    await browser.openUrlRequest(
        urlRequest: URLRequest(
      url: Uri.parse(url),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile Details'),
      ),
      body: Center(
          child: Container(
        padding: const EdgeInsets.all(16.0),
        child: GetBuilder<AuthController>(builder: (authController) {
          return GetBuilder<ProfileController>(builder: (profileController) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                CustomImage(
                  placeholder: Images.placeholder,
                  image:
                      '${Get.find<SplashController>().baseUrls.kitchenProfileUrl}'
                      '/${authController.profileModel.profile!.image}',
                  height: 200,
                  width: 200,
                ),
                const SizedBox(height: 16),

                // Bold and Bigger Font Name
                Text(
                  '${authController.profileModel.profile!.fName!} ${authController.profileModel.profile!.lName!}',
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),

                // Other Texts Vertically Aligned
                Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Text(
                        ' ${'branch'.tr} : ${authController.profileModel.branch!.name!} ',
                        style: robotoRegular.copyWith(
                            fontSize: Dimensions.fontSizeLarge,
                            color: Theme.of(context).primaryColor)),
                    Text(authController.profileModel.profile!.email!,
                        style: robotoMedium.copyWith()),
                    Text(authController.profileModel.profile!.phone!,
                        style: robotoMedium.copyWith()),

                    const SizedBox(height: 36),
                    // Add Bank Account Button
                    // !profileController.isLoading ? ElevatedButton(
                    //   onPressed: () async {
                    //     // Add Bank Account logic goes here
                    //     await profileController.getAddAccountDetails();
                    //       if (profileController.addAccountDetailsModel.data?.url != null) {
                    //         _loadStripe(profileController.addAccountDetailsModel.data!.url!);
                    //       }
                    //     },
                    //   child: Text('Add Bank Account'),
                    // ) : const SizedBox.shrink(),

                    profileController.isLoading ? Center(child: Padding(
                      padding: const EdgeInsets.all(Dimensions.iconSize),
                      child: CircularProgressIndicator(valueColor: AlwaysStoppedAnimation<Color>(Theme.of(context).primaryColor)),
                    )) : const SizedBox.shrink(),
                  ],
                ),
              ],
            );
          });
        }),
      )),
    );
  }
}
