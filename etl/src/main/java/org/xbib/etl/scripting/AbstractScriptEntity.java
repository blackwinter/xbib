/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.etl.scripting;

import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.xbib.etl.Entity;
import org.xbib.etl.EntityQueue;

public abstract class AbstractScriptEntity implements Entity {

    private final ScriptEngine engine;

    private final String invocable;

    private final String script;

    private Entity entity;

    private Map settings;

    public AbstractScriptEntity(String scriptEngineName, String script, String invocable) {
        this.engine = new ScriptEngineManager().getEngineByName(scriptEngineName);
        this.invocable = invocable;
        this.script = script;        
    }

    @Override
    public AbstractScriptEntity setSettings(Map<String,Object> settings) {
        try {
            this.settings = settings;
            engine.eval(script);
            this.entity = (Entity)engine.get(invocable);
            if (entity != null) {
                entity.setSettings(settings);
            }
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public Map getSettings() {
        return settings;
    }

    public Entity getEntity(){
        return entity;
    }

    public AbstractScriptEntity build(EntityQueue.EntityWorker worker, Object key, Object value) {
        Invocable inv = (Invocable) engine;
        try {
            inv.invokeMethod(entity, "complete", worker, key, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

}
