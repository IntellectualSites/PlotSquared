package com.plotsquared.sponge.uuid;

import java.util.UUID;

import org.spongepowered.api.profile.GameProfile;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.SpongeMain;

public class SpongeUUIDHandler extends UUIDHandlerImplementation {
    
    public SpongeUUIDHandler(final UUIDWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    public boolean startCaching(final Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        return cache(whenDone);
    }
    
    public boolean cache(final Runnable whenDone) {
        add(new StringWrapper("*"), DBFunc.everyone);
        for (final GameProfile profile : SpongeMain.THIS.getResolver().getCachedProfiles()) {
            add(new StringWrapper(profile.getName()), profile.getUniqueId());
        }
        return true;
    }
    
    @Override
    public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                ifFetch.value = uuidWrapper.getUUID(name);
                TaskManager.runTask(ifFetch);
            }
        });
    }
    
}
