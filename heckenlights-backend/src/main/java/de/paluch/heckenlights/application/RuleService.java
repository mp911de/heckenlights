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

import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.Rules;

/**
 * @author Mark Paluch
 */
@Component
public class RuleService {

    private final Resource ruleLocation;
    private final Rules rules = new Rules();

    private long lastModified;

    public RuleService(@Value("${rules.location}") Resource ruleLocation) throws IOException {

        this.ruleLocation = ruleLocation;
        this.lastModified = ruleLocation.lastModified();

        updateRules();
    }

    public boolean isChanged() {
        try {
            return lastModified != ruleLocation.lastModified();
        } catch (IOException e) {
            return false;
        }
    }

    public void updateRules() throws IOException {

        Rules updated = JAXB.unmarshal(ruleLocation.getFile(), Rules.class);
        lastModified = ruleLocation.lastModified();
        updateRules(rules, updated);
    }

    public final Rules getRules() {
        return rules;
    }

    private void updateRules(Rules instance, Rules updated) {
        instance.getRules().clear();
        instance.getRules().addAll(updated.getRules());

        instance.setDefaultAction(updated.getDefaultAction());
        instance.setTimeunit(updated.getTimeunit());
        instance.setTimezone(updated.getTimezone());
    }
}
