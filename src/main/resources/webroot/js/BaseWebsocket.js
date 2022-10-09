var BaseWebSocketOption = {
  _websocket : null,
  _message : null,
  tryCount : 0,
  handler : function(text) {
    console.log(text);
  },
  send : function(websocket, message) {
    this._websocket = websocket;
    this._message = message;
    if (this._websocket.readyState === WebSocket.OPEN) {
      this._websocket.send(this._message);
    }
    else{
      this.tryCount = 0;
      this.sendTimeout(); // 延迟200ms，再次发送
    }
  },
  sendTimeout : function() {
    if (BaseWebSocketOption._websocket.readyState === WebSocket.OPEN) {
      console.log('sendTimeout:'+BaseWebSocketOption._message);
      BaseWebSocketOption._websocket.send(BaseWebSocketOption._message);
    } else {
      if (BaseWebSocketOption.tryCount < 5) {
        BaseWebSocketOption.tryCount++;
        console.log('tryCount:'+BaseWebSocketOption.tryCount);
        setTimeout(BaseWebSocketOption.sendTimeout, 200); // 延迟200ms，再次发送
      }
    }
  }
};

function BaseWebSocket(websocketUrl) {
  this._websocket = null;
  this._handler = null;
  //连接发生错误的回调方法
  this.onerror = function() {
    console.log('onerror');
    //BaseWebSocketOption.handler("WebSocket Connect Error.");
  };
  //连接成功建立的回调方法
  this.onopen = function() {
    console.log('onopen');
    //BaseWebSocketOption.handler("WebSocket Connect Successful.");
  };
  //接收到消息的回调方法
  this.onmessage = function(event) {
    console.log('onmessage');
    BaseWebSocketOption.handler(event.data);
  };
  //连接关闭的回调方法
  this.onclose = function() {
    console.log('onclose');
    //BaseWebSocketOption.handler("WebSocket Connect Close.");
  };
  //发送消息
  this.send = function(message) {
    if (this._websocket) {
      BaseWebSocketOption.send(this._websocket, message);
    }
  };
  //关闭WebSocket连接
  this.closeWebSocket = function() {
    if (this._websocket) {
      this._websocket.close();
    }
  };
  this.init = function(websocketUrl) {
    //判断当前浏览器是否支持WebSocket
    //ws://localhost:9003/websocket
    if ('WebSocket' in window) {
      this._websocket = new WebSocket(websocketUrl);
      //连接发生错误的回调方法
      this._websocket.onerror = this.onerror;
      //连接成功建立的回调方法
      this._websocket.onopen = this.onopen;
      //接收到消息的回调方法
      this._websocket.onmessage = this.onmessage;
      //连接关闭的回调方法
      this._websocket.onclose = this.onclose;
    }
    else {
      console.log('Not support websocket')
    }
    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = this.closeWebSocket;
  };
  this.init(websocketUrl);
  return this;
}








