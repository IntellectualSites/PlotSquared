package com.plotsquared.sponge.uuid;

import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.SpongeMain;
import java.util.UUID;
import org.spongepowered.api.profile.GameProfile;

public class SpongeUUIDHandler extends UUIDHandlerImplementation {

    public SpongeUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    public boolean startCaching(Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        return cache();
    }

    public boolean cache() {
        for (GameProfile profile : SpongeMain.THIS.getServer().getGameProfileManager().getCache().getProfiles()) {
            profile.getName()
                .ifPresent(name -> add(new StringWrapper(name), profile.getUniqueId()));
        }
        return true;
    }
    
    @Override
    public void fetchUUID(String name, RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(() -> {
            ifFetch.value = SpongeUUIDHandler.this.uuidWrapper.getUUID(name);
            TaskManager.runTask(ifFetch);
        });
    }
    
}
