/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.paluch.heckenlights.application;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.springframework.util.StringUtils;

/**
 * @author Mark Paluch
 */
class TrackNameUtil {

    public static Optional<String> getSequenceName(Sequence sequence) {

        return Arrays.stream(sequence.getTracks()) //
                .map(TrackNameUtil::getText) //
                .filter(s -> s.isPresent() && StringUtils.hasText(s.get())) //
                .findFirst() //
                .orElse(null);
    }

    private static Optional<String> getText(Track track) {

        for (int i = 0; i < track.size(); i++) {
            MidiEvent midiEvent = track.get(i);
            String text = getText(midiEvent);

            if (StringUtils.hasText(text)) {
                return Optional.of(text);
            }
        }

        return Optional.empty();
    }

    private static String getText(MidiEvent midiEvent) {

        if (midiEvent.getMessage() instanceof MetaMessage) {
            MidiMessageDetail detail = new MidiMessageDetail(midiEvent.getMessage());
            if (detail.getT2() == 3 || detail.getT2() == 6) {

                if (detail.getBytes()[0] == -1) {
                    return new String(detail.getBytes(), 3, detail.getBytes().length - 3, StandardCharsets.US_ASCII);
                }
                return new String(detail.getBytes(), StandardCharsets.US_ASCII);
            }
        }

        return null;
    }
}
