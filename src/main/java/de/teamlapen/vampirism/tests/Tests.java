package de.teamlapen.vampirism.tests;

import com.google.common.base.Stopwatch;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.blocks.BlockCastleBlock;
import de.teamlapen.vampirism.blocks.BlockWeaponTable;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.fluids.BloodHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

/**
 * Since I'm not familiar with JUnit or similar and it does not work that well with Minecraft anyway, this is a some kind of ingame test which is executed via command
 * <p>
 * Usage of lambda and stuff is probably unnecessary and stuff, but fun.
 */
public class Tests {

    public static void runTests(World world, EntityPlayer player) {
        sendMsg(player, "Starting tests");
        log("Clearing area");
        clearArea(world);
        boolean wasCreative = player.isCreative();
        player.setGameType(GameType.SURVIVAL);
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 40, 100));
        player.attemptTeleport(0, 5, 0);
        TestInfo info = new TestInfo(world, player, new BlockPos(-20, 2, -20), "BloodFluidHandler");

        runTest(Tests::bloodFluidHandler, info);
        runTest(Tests::blockWeaponTableFluids, info.next("BlockWeaponTableFluids"));
        runLightTest(Tests::checkObjectHolders, "Object holders", player);

        log("Finished tests -> teleporting player");
        player.attemptTeleport(0, 5, 0);
        if (wasCreative) player.setGameType(GameType.CREATIVE);
        sendMsg(player, "Finished tests");
    }

    private static void runTest(Tester tester, TestInfo info) {
        boolean result;
        try {
            result = tester.run(info);
        } catch (Throwable t) {
            log(info.name + " failed with exception %s", t);
            result = false;
        }
        sendMsg(info.player, info.name + " test " + (result ? "§2was successful§r" : "§4failed§r"));
    }

    private static void runLightTest(LightTester tester, String name, @Nullable EntityPlayer player) {
        boolean result;
        try {
            result = tester.run();
        } catch (Throwable t) {
            log(name + " failed with exception %s", t);
            result = false;
        }
        if (player != null) {
            sendMsg(player, name + " test " + (result ? "§2was successful§r" : "§4failed§r"));
        } else {
            log(name + "test " + (result ? "was successful" : "failed"));
        }
    }

    /**
     * Should be run in POST INIT
     */
    public static void runBackgroundTests() {
        log("Running background tests");
        Stopwatch w = Stopwatch.createStarted();
        runLightTest(Tests::checkObjectHolders, "Object holders", null);
        log("Finished background tests after %s ms", w.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    private static boolean checkObjectHolders() {
        boolean failed;
        failed = !checkObjectHolders(ModBiomes.class);
        failed |= !checkObjectHolders(ModBlocks.class);
        failed |= !checkObjectHolders(ModEnchantments.class);
        failed |= !checkObjectHolders(ModEntities.class);
        failed |= !checkObjectHolders(ModFluids.class);
        failed |= !checkObjectHolders(ModItems.class);
        failed |= !checkObjectHolders(ModPotions.class);
        failed |= !checkObjectHolders(ModSounds.class);
        return !failed;
    }

    private static boolean checkObjectHolders(@Nonnull Class clazz) {
        boolean failed = false;
        for (Field f : clazz.getFields()) {
            int mods = f.getModifiers();
            boolean isMatch = Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods);
            if (!isMatch) {
                continue;
            }
            try {
                if (f.get(null) == null) {
                    log("Field %s in class %s is null", f.getName(), clazz.getName());
                    failed = true;
                }
            } catch (IllegalAccessException e) {
                VampirismMod.log.e("TEST", e, "Failed to check fields of class %s", clazz.getName());
                return false;
            }

        }
        return !failed;
    }

    private static boolean bloodFluidHandler(TestInfo info) {
        info.world.setBlockState(info.pos, ModBlocks.blood_container.getDefaultState());
        TileEntity t = info.world.getTileEntity(info.pos);
        IFluidHandler handler = t.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.random(info.world.rand));
        handler.fill(new FluidStack(ModFluids.blood, 10000000), true);
        int blood = BloodHelper.getBlood(handler);
        assert blood > 0 : "Could not fill blood container";

        ItemStack bloodBottle1 = new ItemStack(ModItems.blood_bottle);
        ItemStack bloodBottle2 = new ItemStack(ModItems.blood_bottle);
        FluidActionResult result1 = FluidUtil.tryFillContainer(bloodBottle1, handler, Integer.MAX_VALUE, null, true);
        assert result1.isSuccess() : "Transaction 1 failed";
        bloodBottle1 = result1.getResult();
        FluidActionResult result2 = FluidUtil.tryFillContainer(bloodBottle2, handler, Integer.MAX_VALUE, null, true);
        assert result2.isSuccess() : "Transaction 2 failed";
        bloodBottle2 = result2.getResult();
        assert BloodHelper.getBlood(handler) < blood : "Failed to drain from container into bottles";
        FluidActionResult result3 = FluidUtil.tryEmptyContainer(bloodBottle1, handler, Integer.MAX_VALUE, null, true);
        assert result3.isSuccess() : "Transaction 3 failed";
        bloodBottle1 = result3.getResult();
        FluidActionResult result4 = FluidUtil.tryEmptyContainer(bloodBottle2, handler, Integer.MAX_VALUE, null, true);
        assert result4.isSuccess() : "Transaction 4 failed";
        bloodBottle2 = result4.getResult();
        log("%d %d", BloodHelper.getBlood(handler), blood);
        assert BloodHelper.getBlood(handler) == blood : "Lost blood somewhere";
        return true;

    }

    private static boolean blockWeaponTableFluids(TestInfo info) {
        info.world.setBlockState(info.pos, ModBlocks.weapon_table.getDefaultState());
        info.player.setHeldItem(info.player.getActiveHand(), new ItemStack(Items.LAVA_BUCKET));
        IBlockState block = info.world.getBlockState(info.pos);
        block.getBlock().onBlockActivated(info.world, info.pos, block, info.player, info.player.getActiveHand(), EnumFacing.random(info.world.rand), 0, 0, 0);
        block = info.world.getBlockState(info.pos);
        assert info.player.getHeldItem(info.player.getActiveHand()).getItem().equals(Items.BUCKET) : "Incorrect Fluid Container Handling";
        log("Block lava level: %s", block.getValue(BlockWeaponTable.LAVA));
        assert (block.getValue(BlockWeaponTable.LAVA) * BlockWeaponTable.MB_PER_META) == Fluid.BUCKET_VOLUME : "Incorrect Fluid Transaction";
        return true;
    }

    private static void log(String msg, Object... format) {
        VampirismMod.log.w("TEST", msg, format);
    }

    private static void sendMsg(EntityPlayer player, String msg) {
        player.sendMessage(new TextComponentString("§1[V-TEST]§r " + msg));
    }

    private static void clearArea(World world) {
        for (int x = -21; x < 22; x++) {
            for (int y = 1; y < 22; y++) {
                for (int z = -21; z < 22; z++) {
                    IBlockState s = (y == 1 || x == -21 || x == 21 || z == -21 || z == 21 || y == 21) ? ModBlocks.castle_block.getDefaultState().withProperty(BlockCastleBlock.VARIANT, BlockCastleBlock.EnumType.DARK_STONE) : Blocks.AIR.getDefaultState();
                    world.setBlockState(new BlockPos(x, y, z), s);
                }
            }
        }
    }

    @FunctionalInterface
    private interface Tester {
        /**
         * Runs the given test
         *
         * @param t the function argument
         * @return the function result
         */
        Boolean run(TestInfo t) throws Throwable;
    }

    @FunctionalInterface
    private interface LightTester {
        /**
         * Runs the given test
         *
         * @return Success
         * @throws Throwable any exception
         */
        Boolean run() throws Throwable;
    }

    private static class TestInfo {
        final World world;
        final EntityPlayer player;
        BlockPos pos;
        String name;

        private TestInfo(World world, EntityPlayer player, BlockPos pos, String name) {
            this.world = world;
            this.player = player;
            this.pos = pos;
            this.name = name;
        }


        private TestInfo next(String name) {
            int x = pos.getX();
            int z = pos.getZ();
            x += 5;
            if (x > 20) {
                x = -20;
                z += 5;
                if (z > 20) {
                    throw new IllegalStateException("Not enough room -> Too many tests");
                }
            }
            this.pos = new BlockPos(x, pos.getY(), z);
            this.name = name;
            return this;
        }

    }
}
