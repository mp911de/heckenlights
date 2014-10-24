package de.paluch.heckenlights

import org.springframework.stereotype.Component
import javax.sound.midi._
import de.paluch.heckenlights.application.MidiMessageDetail

/**
 * This is mostly for fun reasons scala.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 17.02.14 20:11
 */
@Component
class EnqueueS {

  var ASCII: String = "ASCII"

  private def getText(track: Track): String = {
    for (i <- 0 until track.size) {
      {
        val midiEvent: MidiEvent = track.get(i)
        val text = getText(midiEvent)

        if (text != null) return text
      }
    }

    return null;
  }

  private def getText(midiEvent: MidiEvent): String = {
    if (midiEvent.getMessage.isInstanceOf[MetaMessage]) {
      val detail: MidiMessageDetail = new MidiMessageDetail(midiEvent.getMessage)

      if (detail.getT2 == 3 || detail.getT2 == 6) {

        if (detail.getBytes().apply(0) == -1) {
          return new String(detail.getBytes, 3, detail.getBytes.length - 3, ASCII)
        }
        return new String(detail.getBytes, ASCII)
      }
    }

    return null

  }

  def getSequenceName(sequence: Sequence): String = {
    val list = sequence.getTracks().toList


    val textTrack: Option[Track] = list.find(t => getText(t) != null)
    return textTrack.collect {
      case i => getText(i)
    } getOrElse("")
  }
}
