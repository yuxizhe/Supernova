(function(window) {
  if (!window.supernovaJsHandler) {
    return;
  }
  var jsHandler = window.supernovaJsHandler;
  var callbacks = {};

  function handleCallback(callbackID, params) {
    window[callbackID](params);
  }

  window.supernovaBridge = {
    call: function(method, options) {
      if (options) {
        var handlerName = "func_" + new Date().getTime();
        callbacks[handlerName] = {
          success: options['success'],
          fail: options['fail']
        };

        delete options.success;
        delete options.fail;
        jsHandler.execute(method, JSON.stringify(options), handlerName);
      } else {
        jsHandler.execute(method, null, null);
      }
    },
    callback: function(handlerName, isSuccess, res) {
      const handler = callbacks[handlerName];
      if (handler) {
        if (isSuccess) {
          if (handler['success']) {
            var obj = null;
            try {
              obj = JSON.parse(res);
            } catch (e) {
              obj = res;
            }
            handler['success'](obj);
          }
        } else {
          if (handler['fail']) {
            handler['fail'](res);
          }
        }
      }
    }

  }
})(window)
