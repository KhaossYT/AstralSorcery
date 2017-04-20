/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.EffectHandler;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.effect.compound.CompoundEffectSphere;
import hellfirepvp.astralsorcery.client.event.ClientRenderEventHandler;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.MultiBlockArrays;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileCelestialGateway
 * Created by HellFirePvP
 * Date: 16.04.2017 / 17:59
 */
public class TileCelestialGateway extends TileEntityTick {

    private boolean hasMultiblock = false;
    private boolean doesSeeSky = false;

    private Object clientSphere = null;

    @Override
    public void update() {
        super.update();

        if(world.isRemote) {
            playEffects();
        } else {
            if((ticksExisted & 15) == 0) {
                updateSkyState(world.canSeeSky(getPos().up()));
            }

            if((ticksExisted & 15) == 0) {
                updateMultiblockState(MultiBlockArrays.patternCelestialGateway.matches(world, pos));
            }
        }
    }

    private void updateMultiblockState(boolean matches) {
        boolean update = hasMultiblock != matches;
        this.hasMultiblock = matches;
        if(update) {
            markForUpdate();
        }
    }

    private void updateSkyState(boolean seeSky) {
        boolean update = doesSeeSky != seeSky;
        this.doesSeeSky = seeSky;
        if(update) {
            markForUpdate();
        }
    }

    public boolean hasMultiblock() {
        return hasMultiblock;
    }

    public boolean doesSeeSky() {
        return doesSeeSky;
    }

    @SideOnly(Side.CLIENT)
    private void playEffects() {
        if(hasMultiblock && doesSeeSky) {
            Vector3 sphereVec = new Vector3(pos).add(0.5, 2.62, 0.5);
            if(clientSphere == null) {
                CompoundEffectSphere sphere = new CompoundEffectSphere(sphereVec.clone(), Vector3.RotAxis.Y_AXIS, 6, 8, 10);
                sphere.setRemoveIfInvisible(true).setAlphaFadeDistance(4);
                EffectHandler.getInstance().registerFX(sphere);
                clientSphere = sphere;
            }
            double playerDst = new Vector3(Minecraft.getMinecraft().player).distance(sphereVec);
            if(clientSphere != null) {
                if(!((CompoundEffectSphere) clientSphere).getPosition().equals(sphereVec)) {
                    ((CompoundEffectSphere) clientSphere).requestRemoval();

                    CompoundEffectSphere sphere = new CompoundEffectSphere(sphereVec.clone(), Vector3.RotAxis.Y_AXIS, 6, 5, 8);
                    sphere.setRemoveIfInvisible(true).setAlphaFadeDistance(4);
                    EffectHandler.getInstance().registerFX(sphere);
                    clientSphere = sphere;
                }
                if(((EntityComplexFX) clientSphere).isRemoved() && playerDst < 5) {
                    EffectHandler.getInstance().registerFX((EntityComplexFX) clientSphere);
                }
            }
            if(playerDst < 5.5) {
                Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
            }
            if(playerDst < 2.5) {
                EffectHandler.getInstance().requestGatewayUIFor(world, sphereVec, 5.5);
            }
        } else {
            if(clientSphere != null) {
                if(!((EntityComplexFX) clientSphere).isRemoved()) {
                    ((EntityComplexFX) clientSphere).requestRemoval();
                }
            }
        }
    }

    @Override
    protected void onFirstTick() {
        if(world.isRemote) return;

        GatewayCache cache = WorldCacheManager.getOrLoadData(world, WorldCacheManager.SaveKey.GATEWAY_DATA);
        cache.offerPosition(world, pos);
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.hasMultiblock = compound.getBoolean("mbState");
        this.doesSeeSky = compound.getBoolean("skyState");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setBoolean("mbState", this.hasMultiblock);
        compound.setBoolean("skyState", this.doesSeeSky);
    }

}
