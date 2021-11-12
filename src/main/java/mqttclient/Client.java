package mqttclient;

import java.security.Timestamp;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Client {
	
	IMqttClient cli;
	MqttMessage msg;
	MemoryPersistence persistence = new MemoryPersistence();
	String broker = "tcp://utfpr2.iotrixx.com.br:1884";
	String publisherId = UUID.randomUUID().toString();
	String subscriberId = UUID.randomUUID().toString();
	String topic = "/sensor/temperatura";
	
	void setConnection() {
		
		try {
			cli = new MqttClient(broker,publisherId,persistence);
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		try {
			cli.connect(options);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void publish(String message) {
		msg = new MqttMessage(message.getBytes());
		try {
			cli.publish(topic, msg);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void subscribe() {
		// Latch used for synchronizing b/w threads
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Callback - Anonymous inner-class for receiving messages
        cli.setCallback(new MqttCallback() {

            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("\nMensagem recebida!" + 
                        "\n\tTopico:   " + topic + 
                        "\n\tMensagem: " + new String(message.getPayload()));
                latch.countDown(); // unblock main thread
            }

            public void connectionLost(Throwable cause) {
                System.out.println("Conexao perdida!" + cause.getMessage());
                latch.countDown();
            }

			public void deliveryComplete(IMqttDeliveryToken token) {
				// TODO Auto-generated method stub
				
			}
        });
        
     // Subscribe client to the topic filter and a QoS level of 0
        System.out.println("Inscrevendo-se no topico: " + topic);
        try {
			cli.subscribe(topic, 0);
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        System.out.println("Inscrito");

        // Wait for the message to be received
        try {
            latch.await(); // block here until message received, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("Fui acordado!");
        }
        
	}
	
	public static void main(String args[]) {
		Client pub = new Client();
		pub.setConnection();
		pub.subscribe();
		/*while (true) {
			pub.publish("teste");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
	}
	
}
