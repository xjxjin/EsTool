/**
 * @description 写cookie，设置全局参数
 * @param {string} _sName 全局参数名称
 * @param {string} _sValue 全局参数名称对于的值
 */
function setGlobalVar(_sName, _sValue) {
  _sValue = _sValue + "";
  document.cookie = escape(_sName) + "=" + escape(_sValue) + '; path=/';
}

/**
 * @description 读cookie，获取全局参数
 * @param {string} _sName 全局参数名称（对应setGlobalVar方法中的_sName）
 * @return {string} result 返回值（对应setGlobalVar方法中的_sValue）
 */
function getGlobalVar(_sName) {
  var result = "";
  var aCookie = document.cookie.split("; ");
  for (var i = 0; i < aCookie.length; i++) {
    var aCrumb = aCookie[i].split("=");
    if (escape(_sName) == aCrumb[0]) {
      result = unescape(aCrumb[1]);
      break;
    }
  }
  return result;
}

var CommUtil = {
  showNumber : function(value) {
    var result = "";
    var valStr = value + "";
    if (valStr.length == 0) {
      return result;
    }
    var first = valStr.length % 4;
    if (first > 0) {
      result += "," + valStr.substr(0, first);
    }
    while(first < valStr.length) {
      result += "," + valStr.substr(first, 4);
      first += 4;
    }
    return result.substr(1);
  }
}

String.prototype.format = function (args) {
  var result = this;
  if (arguments.length < 1) {
    return result;
  }

  var data = arguments; //如果模板参数是数组
  if (arguments.length == 1 && typeof(args) == "object") {
    //如果模板参数是对象
    data = args;
  }
  for (var key in data) {
    var value = data[key];
    if (undefined != value) {
      result = result.replace("{" + key + "}", value);
    }
  }
  return result;
}