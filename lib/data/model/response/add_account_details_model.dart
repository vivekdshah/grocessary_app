
class AddAccountDetailsModel {
  String? status;
  AccountLinkData? data;

  AddAccountDetailsModel({this.status, this.data});

  AddAccountDetailsModel.fromJson(Map<String, dynamic> json) {
    status = json['status'];
    data = json['data'] != null ? AccountLinkData.fromJson(json['data']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = <String, dynamic>{};
    data['status'] = status;
    if (this.data != null) {
      data['data'] = this.data!.toJson();
    }
    return data;
  }
}

class AccountLinkData {
  String? object;
  int? created;
  int? expiresAt;
  String? url;

  AccountLinkData({
    this.object,
    this.created,
    this.expiresAt,
    this.url,
  });

  AccountLinkData.fromJson(Map<String, dynamic> json) {
    object = json['object'];
    created = json['created'];
    expiresAt = json['expires_at'];
    url = json['url'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = <String, dynamic>{};
    data['object'] = object;
    data['created'] = created;
    data['expires_at'] = expiresAt;
    data['url'] = url;
    return data;
  }
}