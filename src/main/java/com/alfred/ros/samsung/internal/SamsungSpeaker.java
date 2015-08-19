/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.alfred.ros.samsung.internal;

import media_msgs.MediaAction;
import media_msgs.SpeakerInfo;
import media_msgs.StateData;
import media_msgs.ToggleMuteSpeakerRequest;
import media_msgs.ToggleMuteSpeakerResponse;

import com.alfred.ros.media.ISpeaker;

/**
*
* @author Mickael Gaillard <mick.gaillard@gmail.com>
*
*/
public class SamsungSpeaker implements ISpeaker {

    @Override
    public void load(SpeakerInfo stateData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void callbackCmdAction(MediaAction message, StateData stateData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleSpeakerMuteToggle(ToggleMuteSpeakerRequest request,
            ToggleMuteSpeakerResponse response) {
        // TODO Auto-generated method stub

    }


}
