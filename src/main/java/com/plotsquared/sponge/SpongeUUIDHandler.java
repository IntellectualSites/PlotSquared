package com.plotsquared.sponge;

import java.util.Collection;
import java.util.UUID;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.service.profile.GameProfileResolver;

import com.google.inject.Inject;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class SpongeUUIDHandler extends UUIDHandlerImplementation {

    public SpongeUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public boolean startCaching(Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        return cache(whenDone);
    }

    public boolean cache(Runnable whenDone) {
        // TODO cache UUIDS
//        SpongeMain.THIS.getRegistry().get
        add(new StringWrapper("*"), DBFunc.everyone);
        for (GameProfile profile : SpongeMain.THIS.getResolver().getCachedProfiles()) {
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
