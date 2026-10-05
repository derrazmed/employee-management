import { Client } from '@stomp/stompjs'

const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'

const brokerURL = `${protocol}//${window.location.host}/ws`

let client = null

export const notificationSocket = {
  connect(onNotification) {
    // Prevent multiple WebSocket connections
    if (client?.active) {
      return
    }

    client = new Client({
      brokerURL,

      reconnectDelay: 5000,

      debug: (message) => {
        console.log('[STOMP]', message)
      },

      onConnect: () => {
        console.log('WebSocket connected')

        client.subscribe('/user/queue/notifications', (message) => {
          try {
            const notification = JSON.parse(message.body)

            console.log('Received notification:', notification)

            onNotification(notification)
          } catch (error) {
            console.error('Failed to parse notification:', error)
          }
        })
      },

      onDisconnect: () => {
        console.log('WebSocket disconnected')
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])

        console.error('Details:', frame.body)
      },

      onWebSocketError: (error) => {
        console.error('WebSocket error:', error)
      },
    })

    client.activate()
  },

  disconnect() {
    if (!client) {
      return
    }

    client.deactivate()
    client = null

    console.log('WebSocket disconnected')
  },

  isConnected() {
    return client?.connected ?? false
  },
}
