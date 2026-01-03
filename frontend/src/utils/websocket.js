import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
    this.subscriptions = new Map();
  }

  connect(guestId, onConnected, onError) {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    // 动态获取WebSocket地址，根据当前环境自动切换
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
    const host = window.location.host; // 自动获取当前域名和端口
    const wsUrl = `${protocol}//${host}/api/ws?guestId=${guestId}`;
    
    console.log('WebSocket connecting to:', wsUrl);
    const socket = new SockJS(wsUrl);
    this.stompClient = Stomp.over(socket);
    
    // 开启调试日志（临时用于排查问题）
    this.stompClient.debug = (str) => {
      console.log('[STOMP Debug]', str);
    };

    this.stompClient.connect(
      {},
      (frame) => {
        console.log('WebSocket Connected:', frame);
        this.connected = true;
        this.reconnectAttempts = 0;
        
        // 订阅用户特定的消息队列
        this.subscribeToUserQueue(guestId);
        
        if (onConnected) onConnected();
      },
      (error) => {
        console.error('WebSocket Connection Error:', error);
        this.connected = false;
        
        if (onError) onError(error);
        
        // 自动重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          console.log(`Reconnecting... Attempt ${this.reconnectAttempts}`);
          setTimeout(() => {
            this.connect(guestId, onConnected, onError);
          }, this.reconnectDelay);
        }
      }
    );
  }

  subscribeToUserQueue(guestId) {
    if (!this.stompClient || !this.connected) {
      console.error('Cannot subscribe: WebSocket not connected');
      return;
    }

    // Spring WebSocket会自动将/user/queue/*路由到当前用户
    // 不需要在路径中指定guestId
    const destination = `/user/queue/recruitment`;
    
    const subscription = this.stompClient.subscribe(destination, (message) => {
      try {
        const notification = JSON.parse(message.body);
        console.log('Received notification:', notification);
        
        // 触发自定义事件
        window.dispatchEvent(new CustomEvent('recruitment-notification', {
          detail: notification
        }));
      } catch (error) {
        console.error('Error parsing notification:', error);
      }
    });

    this.subscriptions.set('recruitment', subscription);
    console.log(`Subscribed to ${destination} for user ${guestId}`);
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      // 取消所有订阅
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();
      
      this.stompClient.disconnect(() => {
        console.log('WebSocket Disconnected');
        this.connected = false;
      });
    }
  }

  isConnected() {
    return this.connected;
  }
}

// 单例模式
const websocketService = new WebSocketService();

export default websocketService;
