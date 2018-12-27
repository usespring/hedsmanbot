package io.github.headsmanbot;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by abbas on 12/27/2018.
 */
@XmlRootElement
//// TODO: 12/27/2018 move to proper place 
public class Expressions {
    @XmlElement
    Map<String, Long> expressionAndAdminChatId = new TreeMap<>();
}
