/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.samsung;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.ros2.rcljava.RCLJava;
import org.ros2.rcljava.node.Node;
import org.rosbuilding.common.BaseDriverNode;
import org.rosbuilding.common.media.MediaMessageConverter;
import org.rosbuilding.common.media.MediaStateDataComparator;
import org.rosmultimedia.player.samsung.driver.LcdTvC650;
import org.rosmultimedia.player.samsung.driver.SamsungRemoteSession;
import org.rosmultimedia.player.samsung.internal.SamsungMonitor;
import org.rosmultimedia.player.samsung.internal.SamsungPlayer;
import org.rosmultimedia.player.samsung.internal.SamsungSystem;

import smarthome_media_msgs.msg.MediaAction;
import smarthome_media_msgs.msg.StateData;

/**
 * SamsungTv ROS Node.
 *
 * @author Mickael Gaillard <mick.gaillard@gmail.com>
 *
 */
public class SamsungTvNode extends BaseDriverNode<SamsungConfig, StateData, MediaAction> {

    protected SamsungRemoteSession tvIp;
    private static Logger logger = Logger.getLogger(RCLJava.LOG_NAME);

    public SamsungTvNode() {
        super("samsungtv",
                new MediaStateDataComparator(),
                new MediaMessageConverter(),
                MediaAction.class.getName(),
                StateData.class.getName());
    }

    @Override
    public void onStart(Node connectedNode) {
        super.onStart(connectedNode);
        this.tvIp = new LcdTvC650(this);
        this.startFinal();
    }

    @Override
    public void onShutdown(Node node) {
        this.tvIp.destroy();
        super.onShutdown(node);
    }

    @Override
    protected void onConnected() {
        this.getStateData().setState(StateData.ENABLE);
    }

    @Override
    protected void onDisconnected() {
        this.getStateData().setState(StateData.UNKNOWN);
    }

//    @Override
//    public void onNewMessage(MediaAction message) {
//        if (message != null) {
//            this.logI(String.format("Command \"%s\"... for %s",
//                    message.getMethod(),
//                    message.getUri()));
//
//            super.onNewMessage(message);
//        }
//    }

    @Override
    protected boolean connect() {
        boolean isConnected = false;
        this.logI(String.format("Connecting to %s:%s...", this.configuration.getHost(), this.configuration.getPort()));

        try {
            this.tvIp = SamsungRemoteSession.create(
                    this,
                    SamsungRemoteSession.APP,
                    SamsungRemoteSession.REMOTE,
                    this.configuration.getHost(),
                    this.configuration.getPort() );

            this.getStateData().setState(StateData.INIT);
            isConnected = true;
            this.logI("\tConnected done.");
        } catch (Exception e) {
            this.getStateData().setState(StateData.SHUTDOWN);
            try {
                Thread.sleep(10000 / this.configuration.getRate());
            } catch (InterruptedException ex) {
                this.logE(ex);
            }
        }

        return isConnected;
    }

    @Override
    protected void initialize() {
        super.initialize();

//        this.speaker = new SamsungSpeaker(this);
//        this.library = new SamsungLibrary(this);

        this.addModule(new SamsungMonitor(this));
        this.addModule(new SamsungPlayer(this));
        this.addModule(new SamsungSystem(this));
    }

    public void pushEnum (final SamsungCommand key, final float timeout )
            throws IOException, InterruptedException {
        this.logD("Send to Samsung : " + key.getValue() );
        this.tvIp.sendKey( key );
        Thread.sleep((long) (1000l*timeout));

    }

    public void pushEnum (final SamsungCommand key)
            throws IOException, InterruptedException {
        this.pushEnum( key, 0.250f );
    }

    public void sendText (final String text, final float timeout )
            throws IOException, InterruptedException {
        this.tvIp.sendText(text);
        Thread.sleep((long) (1000l*timeout));
    }

    public void sendText (final String text)
            throws IOException, InterruptedException {
        this.sendText( text, 0.250f );
    }

    public static boolean isInteger(final String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    @Override
    protected SamsungConfig getConfig() {
        return new SamsungConfig(this.getConnectedNode());
    }

    public static void main(String[] args) throws InterruptedException {
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        handler.setLevel(Level.ALL);

        // Initialize RCL
        RCLJava.rclJavaInit();

        // Let's create a Node
        Node node = RCLJava.createNode("samsungtv");

        SamsungTvNode samsung = new SamsungTvNode();
        samsung.onStart(node);

        RCLJava.spin(node);

        samsung.onShutdown(node);
        node.dispose();
        RCLJava.shutdown();
    }
}
