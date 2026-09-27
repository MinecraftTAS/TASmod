package com.minecrafttas.tasmod.savestates.typeadapters;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.minecrafttas.mctcommon.json.FineField.FineMode;
import com.minecrafttas.mctcommon.json.FineMultiTarget;
import com.minecrafttas.mctcommon.json.FineTarget;
import com.minecrafttas.mctcommon.json.FineTypeAdapter;

import net.minecraft.item.Item;

@FineMultiTarget
public class EntityAITypeAdapters {

	@FineTarget(net.minecraft.entity.ai.EntityAIBase.class)
	public static class EntityAIBaseTypeAdapter extends FineTypeAdapter {

		public EntityAIBaseTypeAdapter() {
			register("mutexBits", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMoveTowardsRestriction.class)
	public static class EntityAIMoveTowardsRestrictionTypeAdapter extends FineTypeAdapter {

		public EntityAIMoveTowardsRestrictionTypeAdapter() {
			register("creature", FineMode.FINE);
			register("movePosX", FineMode.FINE);
			register("movePosY", FineMode.FINE);
			register("movePosZ", FineMode.FINE);
			register("movementSpeed", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIWander.class)
	public static class EntityAIWanderTypeAdapter extends FineTypeAdapter {

		public EntityAIWanderTypeAdapter() {
			register("entity", FineMode.FINE);
			register("x", FineMode.FINE);
			register("y", FineMode.FINE);
			register("z", FineMode.FINE);
			register("speed", FineMode.FINE);
			register("executionChance", FineMode.FINE);
			register("mustUpdate", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIWanderAvoidWater.class)
	public static class EntityAIWanderAvoidWaterTypeAdapter extends FineTypeAdapter {

		public EntityAIWanderAvoidWaterTypeAdapter() {
			register("probability", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIWatchClosest.class)
	public static class EntityAIWatchClosestTypeAdapter extends FineTypeAdapter {

		public EntityAIWatchClosestTypeAdapter() {
			register("entity", FineMode.FINE);
			register("closestEntity", FineMode.FINE);
			register("maxDistance", FineMode.FINE);
			register("lookTime", FineMode.FINE);
			register("chance", FineMode.FINE);
			register("watchedClass", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILookIdle.class)
	public static class EntityAILookIdleTypeAdapter extends FineTypeAdapter {

		public EntityAILookIdleTypeAdapter() {
			register("idleEntity", FineMode.FINE);
			register("lookX", FineMode.FINE);
			register("lookZ", FineMode.FINE);
			register("idleTime", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAITarget.class)
	public static class EntityAITargetTypeAdapter extends FineTypeAdapter {

		public EntityAITargetTypeAdapter() {
			register("taskOwner", FineMode.FINE);
			register("shouldCheckSight", FineMode.FINE);
			register("nearbyOnly", FineMode.FINE);
			register("targetSearchStatus", FineMode.FINE);
			register("targetSearchDelay", FineMode.FINE);
			register("targetUnseenTicks", FineMode.FINE);
			register("target", FineMode.FINE);
			register("unseenMemoryTicks", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIHurtByTarget.class)
	public static class EntityAIHurtByTargetTypeAdapter extends FineTypeAdapter {

		public EntityAIHurtByTargetTypeAdapter() {
			register("entityCallsForHelp", FineMode.FINE);
			register("revengeTimerOld", FineMode.FINE);
			register("excludedReinforcementTypes", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAINearestAttackableTarget.class)
	public static class EntityAINearestAttackableTargetTypeAdapter extends FineTypeAdapter {

		public EntityAINearestAttackableTargetTypeAdapter() {
			register("targetClass", FineMode.FINE);
			register("targetChance", FineMode.FINE);
			register("sorter", FineMode.FINE);
			register("targetEntitySelector", FineMode.FINE);
			register("targetEntity", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAISit.class)
	public static class EntityAISitTypeAdapter extends FineTypeAdapter {

		public EntityAISitTypeAdapter() {
			register("tameable", FineMode.FINE);
			register("isSitting", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAITempt.class)
	public static class EntityAITemptTypeAdapter extends FineTypeAdapter {

		public EntityAITemptTypeAdapter() {
			register("temptedEntity", FineMode.FINE);
			register("speed", FineMode.FINE);
			register("targetX", FineMode.FINE);
			register("targetY", FineMode.FINE);
			register("targetZ", FineMode.FINE);
			register("pitch", FineMode.FINE);
			register("yaw", FineMode.FINE);
			register("temptingPlayer", FineMode.FINE);
			register("delayTemptCounter", FineMode.FINE);
			register("isRunning", FineMode.FINE);
			register("temptItem", (obj, fgson) -> {
				JsonArray out = new JsonArray();
				@SuppressWarnings("unchecked")
				Set<Item> in = (Set<Item>) obj;
				for (Item item : in) {
					out.add(fgson.serialize(item, Item.class));
				}
				return out;
			}, (elem, fgson) -> {
				Set<Item> out = new HashSet<>();
				JsonArray in = elem.getAsJsonArray();
				for (JsonElement element : in) {
					out.add((Item) fgson.deserialize(element, Item.class));
				}
				return out;
			});
			register("scaredByPlayerMovement", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAISwimming.class)
	public static class EntityAISwimmingTypeAdapter extends FineTypeAdapter {

		public EntityAISwimmingTypeAdapter() {
			register("entity", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFollowOwner.class)
	public static class EntityAIFollowOwnerTypeAdapter extends FineTypeAdapter {

		public EntityAIFollowOwnerTypeAdapter() {
			register("tameable", FineMode.FINE);
			register("owner", FineMode.FINE);
			register("world", FineMode.FINE);
			register("followSpeed", FineMode.FINE);
			register("petPathfinder", FineMode.FINE);
			register("timeToRecalcPath", FineMode.FINE);
			register("maxDist", FineMode.FINE);
			register("minDist", FineMode.FINE);
			register("oldWaterCost", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMoveToBlock.class)
	public static class EntityAIMoveToBlockTypeAdapter extends FineTypeAdapter {

		public EntityAIMoveToBlockTypeAdapter() {
			register("creature", FineMode.FINE);
			register("movementSpeed", FineMode.FINE);
			register("runDelay", FineMode.FINE);
			register("timeoutCounter", FineMode.FINE);
			register("maxStayTicks", FineMode.FINE);
			register("destinationBlock", FineMode.FINE);
			register("isAboveDestination", FineMode.FINE);
			register("searchLength", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIOcelotSit.class)
	public static class EntityAIOcelotSitTypeAdapter extends FineTypeAdapter {

		public EntityAIOcelotSitTypeAdapter() {
			register("ocelot", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILeapAtTarget.class)
	public static class EntityAILeapAtTargetTypeAdapter extends FineTypeAdapter {

		public EntityAILeapAtTargetTypeAdapter() {
			register("leaper", FineMode.FINE);
			register("leapTarget", FineMode.FINE);
			register("leapMotionY", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIOcelotAttack.class)
	public static class EntityAIOcelotAttackTypeAdapter extends FineTypeAdapter {

		public EntityAIOcelotAttackTypeAdapter() {
			register("world", FineMode.FINE);
			register("entity", FineMode.FINE);
			register("target", FineMode.FINE);
			register("attackCountdown", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMate.class)
	public static class EntityAIMateTypeAdapter extends FineTypeAdapter {

		public EntityAIMateTypeAdapter() {
			register("animal", FineMode.FINE);
			register("mateClass", FineMode.FINE);
			register("world", FineMode.FINE);
			register("targetMate", FineMode.FINE);
			register("spawnBabyDelay", FineMode.FINE);
			register("moveSpeed", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAITargetNonTamed.class)
	public static class EntityAITargetNonTamedTypeAdapter extends FineTypeAdapter {

		public EntityAITargetNonTamedTypeAdapter() {
			register("tameable", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIAvoidEntity.class)
	public static class EntityAIAvoidEntityTypeAdapter extends FineTypeAdapter {

		public EntityAIAvoidEntityTypeAdapter() {
			register("canBeSeenSelector", FineMode.FINE);
			register("entity", FineMode.FINE);
			register("farSpeed", FineMode.FINE);
			register("nearSpeed", FineMode.FINE);
			register("closestLivingEntity", FineMode.FINE);
			register("avoidDistance", FineMode.FINE);
			register("path", FineMode.FINE);
			register("navigation", FineMode.FINE);
			register("classToAvoid", FineMode.FINE);
			register("avoidTargetSelector", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAICreeperSwell.class)
	public static class EntityAICreeperSwellTypeAdapter extends FineTypeAdapter {

		public EntityAICreeperSwellTypeAdapter() {
			register("swellingCreeper", FineMode.FINE);
			register("creeperAttackTarget", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIAttackMelee.class)
	public static class EntityAIAttackMeleeTypeAdapter extends FineTypeAdapter {

		public EntityAIAttackMeleeTypeAdapter() {
			register("world", FineMode.FINE);
			register("attacker", FineMode.FINE);
			register("attackTick", FineMode.FINE);
			register("speedTowardsTarget", FineMode.FINE);
			register("longMemory", FineMode.FINE);
			register("path", FineMode.FINE);
			register("delayCounter", FineMode.FINE);
			register("targetX", FineMode.FINE);
			register("targetY", FineMode.FINE);
			register("targetZ", FineMode.FINE);
			register("attackInterval", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIPanic.class)
	public static class EntityAIPanicTypeAdapter extends FineTypeAdapter {

		public EntityAIPanicTypeAdapter() {
			register("creature", FineMode.FINE);
			register("speed", FineMode.FINE);
			register("randPosX", FineMode.FINE);
			register("randPosY", FineMode.FINE);
			register("randPosZ", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFollowParent.class)
	public static class EntityAIFollowParentTypeAdapter extends FineTypeAdapter {

		public EntityAIFollowParentTypeAdapter() {
			register("childAnimal", FineMode.FINE);
			register("parentAnimal", FineMode.FINE);
			register("moveSpeed", FineMode.FINE);
			register("delayCounter", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy.class)
	public static class EntityAIRunAroundLikeCrazyTypeAdapter extends FineTypeAdapter {

		public EntityAIRunAroundLikeCrazyTypeAdapter() {
			register("horseHost", FineMode.FINE);
			register("speed", FineMode.FINE);
			register("targetX", FineMode.FINE);
			register("targetY", FineMode.FINE);
			register("targetZ", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer.class)
	public static class EntityAIFindEntityNearestPlayerTypeAdapter extends FineTypeAdapter {

		public EntityAIFindEntityNearestPlayerTypeAdapter() {
			register("LOGGER", FineMode.EXCLUDED);
			register("entityLiving", FineMode.FINE);
			register("predicate", FineMode.FINE);
			register("sorter", FineMode.FINE);
			register("entityTarget", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIZombieAttack.class)
	public static class EntityAIZombieAttackTypeAdapter extends FineTypeAdapter {

		public EntityAIZombieAttackTypeAdapter() {
			register("zombie", FineMode.FINE);
			register("raiseArmTicks", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMoveThroughVillage.class)
	public static class EntityAIMoveThroughVillageTypeAdapter extends FineTypeAdapter {

		public EntityAIMoveThroughVillageTypeAdapter() {
			register("entity", FineMode.FINE);
			register("movementSpeed", FineMode.FINE);
			register("path", FineMode.FINE);
			register("doorInfo", FineMode.FINE);
			register("isNocturnal", FineMode.FINE);
			register("doorList", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIDoorInteract.class)
	public static class EntityAIDoorInteractTypeAdapter extends FineTypeAdapter {

		public EntityAIDoorInteractTypeAdapter() {
			register("entity", FineMode.FINE);
			register("doorPosition", FineMode.FINE);
			register("doorBlock", FineMode.FINE);
			register("hasStoppedDoorInteraction", FineMode.FINE);
			register("entityPositionX", FineMode.FINE);
			register("entityPositionZ", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIBreakDoor.class)
	public static class EntityAIBreakDoorTypeAdapter extends FineTypeAdapter {

		public EntityAIBreakDoorTypeAdapter() {
			register("breakingTime", FineMode.FINE);
			register("previousBreakProgress", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMoveTowardsTarget.class)
	public static class EntityAIMoveTowardsTargetTypeAdapter extends FineTypeAdapter {

		public EntityAIMoveTowardsTargetTypeAdapter() {
			register("creature", FineMode.FINE);
			register("targetEntity", FineMode.FINE);
			register("movePosX", FineMode.FINE);
			register("movePosY", FineMode.FINE);
			register("movePosZ", FineMode.FINE);
			register("speed", FineMode.FINE);
			register("maxTargetDistance", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILookAtVillager.class)
	public static class EntityAILookAtVillagerTypeAdapter extends FineTypeAdapter {

		public EntityAILookAtVillagerTypeAdapter() {
			register("ironGolem", FineMode.FINE);
			register("villager", FineMode.FINE);
			register("lookTime", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIDefendVillage.class)
	public static class EntityAIDefendVillageTypeAdapter extends FineTypeAdapter {

		public EntityAIDefendVillageTypeAdapter() {
			register("irongolem", FineMode.FINE);
			register("villageAgressorTarget", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILlamaFollowCaravan.class)
	public static class EntityAILlamaFollowCaravanTypeAdapter extends FineTypeAdapter {

		public EntityAILlamaFollowCaravanTypeAdapter() {
			register("llama", FineMode.FINE);
			register("speedModifier", FineMode.FINE);
			register("distCheckCounter", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIAttackRanged.class)
	public static class EntityAIAttackRangedTypeAdapter extends FineTypeAdapter {

		public EntityAIAttackRangedTypeAdapter() {
			register("entityHost", FineMode.FINE);
			register("rangedAttackEntityHost", FineMode.EXCLUDED);
			register("attackTarget", FineMode.FINE);
			register("rangedAttackTime", FineMode.FINE);
			register("entityMoveSpeed", FineMode.FINE);
			register("seeTime", FineMode.FINE);
			register("attackIntervalMin", FineMode.FINE);
			register("maxRangedAttackTime", FineMode.FINE);
			register("attackRadius", FineMode.FINE);
			register("maxAttackDistance", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFindEntityNearest.class)
	public static class EntityAIFindEntityNearestTypeAdapter extends FineTypeAdapter {

		public EntityAIFindEntityNearestTypeAdapter() {
			register("LOGGER", FineMode.EXCLUDED);
			register("mob", FineMode.FINE);
			register("predicate", FineMode.FINE);
			register("sorter", FineMode.FINE);
			register("target", FineMode.FINE);
			register("classToCheck", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFollowOwnerFlying.class)
	public static class EntityAIFollowOwnerFlyingTypeAdapter extends FineTypeAdapter {

		public EntityAIFollowOwnerFlyingTypeAdapter() {
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying.class)
	public static class EntityAIWanderAvoidWaterFlyingTypeAdapter extends FineTypeAdapter {

		public EntityAIWanderAvoidWaterFlyingTypeAdapter() {
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILandOnOwnersShoulder.class)
	public static class EntityAILandOnOwnersShoulderTypeAdapter extends FineTypeAdapter {

		public EntityAILandOnOwnersShoulderTypeAdapter() {
			register("entity", FineMode.FINE);
			register("owner", FineMode.FINE);
			register("isSittingOnShoulder", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFollow.class)
	public static class EntityAIFollowTypeAdapter extends FineTypeAdapter {

		public EntityAIFollowTypeAdapter() {
			register("entity", FineMode.FINE);
			register("followPredicate", FineMode.FINE);
			register("followingEntity", FineMode.FINE);
			register("speedModifier", FineMode.FINE);
			register("navigation", FineMode.FINE);
			register("timeToRecalcPath", FineMode.FINE);
			register("stopDistance", FineMode.FINE);
			register("oldWaterCost", FineMode.FINE);
			register("areaSize", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIEatGrass.class)
	public static class EntityAIEatGrassTypeAdapter extends FineTypeAdapter {

		public EntityAIEatGrassTypeAdapter() {
			register("IS_TALL_GRASS", FineMode.FINE);
			register("grassEaterEntity", FineMode.FINE);
			register("entityWorld", FineMode.FINE);
			register("eatingGrassTimer", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIRestrictSun.class)
	public static class EntityAIRestrictSunTypeAdapter extends FineTypeAdapter {

		public EntityAIRestrictSunTypeAdapter() {
			register("entity", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFleeSun.class)
	public static class EntityAIFleeSunTypeAdapter extends FineTypeAdapter {

		public EntityAIFleeSunTypeAdapter() {
			register("creature", FineMode.FINE);
			register("shelterX", FineMode.FINE);
			register("shelterY", FineMode.FINE);
			register("shelterZ", FineMode.FINE);
			register("movementSpeed", FineMode.FINE);
			register("world", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIAttackRangedBow.class)
	public static class EntityAIAttackRangedBowTypeAdapter extends FineTypeAdapter {

		public EntityAIAttackRangedBowTypeAdapter() {
			register("entity", FineMode.FINE);
			register("moveSpeedAmp", FineMode.FINE);
			register("attackCooldown", FineMode.FINE);
			register("maxAttackDistance", FineMode.FINE);
			register("attackTime", FineMode.FINE);
			register("seeTime", FineMode.FINE);
			register("strafingClockwise", FineMode.FINE);
			register("strafingBackwards", FineMode.FINE);
			register("strafingTime", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAISkeletonRiders.class)
	public static class EntityAISkeletonRidersTypeAdapter extends FineTypeAdapter {

		public EntityAISkeletonRidersTypeAdapter() {
			register("horse", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAITradePlayer.class)
	public static class EntityAITradePlayerTypeAdapter extends FineTypeAdapter {

		public EntityAITradePlayerTypeAdapter() {
			register("villager", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAILookAtTradePlayer.class)
	public static class EntityAILookAtTradePlayerTypeAdapter extends FineTypeAdapter {

		public EntityAILookAtTradePlayerTypeAdapter() {
			register("villager", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIMoveIndoors.class)
	public static class EntityAIMoveIndoorsTypeAdapter extends FineTypeAdapter {

		public EntityAIMoveIndoorsTypeAdapter() {
			register("entity", FineMode.FINE);
			register("doorInfo", FineMode.FINE);
			register("insidePosX", FineMode.FINE);
			register("insidePosZ", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIRestrictOpenDoor.class)
	public static class EntityAIRestrictOpenDoorTypeAdapter extends FineTypeAdapter {

		public EntityAIRestrictOpenDoorTypeAdapter() {
			register("entity", FineMode.FINE);
			register("frontDoor", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIOpenDoor.class)
	public static class EntityAIOpenDoorTypeAdapter extends FineTypeAdapter {

		public EntityAIOpenDoorTypeAdapter() {
			register("closeDoor", FineMode.FINE);
			register("closeDoorTemporisation", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIVillagerMate.class)
	public static class EntityAIVillagerMateTypeAdapter extends FineTypeAdapter {

		public EntityAIVillagerMateTypeAdapter() {
			register("villager", FineMode.FINE);
			register("mate", FineMode.FINE);
			register("world", FineMode.FINE);
			register("matingTimeout", FineMode.FINE);
			register("village", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIFollowGolem.class)
	public static class EntityAIFollowGolemTypeAdapter extends FineTypeAdapter {

		public EntityAIFollowGolemTypeAdapter() {
			register("villager", FineMode.FINE);
			register("ironGolem", FineMode.FINE);
			register("takeGolemRoseTick", FineMode.FINE);
			register("tookGolemRose", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIWatchClosest2.class)
	public static class EntityAIWatchClosest2TypeAdapter extends FineTypeAdapter {

		public EntityAIWatchClosest2TypeAdapter() {
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIVillagerInteract.class)
	public static class EntityAIVillagerInteractTypeAdapter extends FineTypeAdapter {

		public EntityAIVillagerInteractTypeAdapter() {
			register("interactionDelay", FineMode.FINE);
			register("villager", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIHarvestFarmland.class)
	public static class EntityAIHarvestFarmlandTypeAdapter extends FineTypeAdapter {

		public EntityAIHarvestFarmlandTypeAdapter() {
			register("villager", FineMode.FINE);
			register("hasFarmItem", FineMode.FINE);
			register("wantsToReapStuff", FineMode.FINE);
			register("currentTask", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIBeg.class)
	public static class EntityAIBegTypeAdapter extends FineTypeAdapter {

		public EntityAIBegTypeAdapter() {
			register("wolf", FineMode.FINE);
			register("player", FineMode.FINE);
			register("world", FineMode.FINE);
			register("minPlayerDistance", FineMode.FINE);
			register("timeoutCounter", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIOwnerHurtByTarget.class)
	public static class EntityAIOwnerHurtByTargetTypeAdapter extends FineTypeAdapter {

		public EntityAIOwnerHurtByTargetTypeAdapter() {
			register("tameable", FineMode.FINE);
			register("attacker", FineMode.FINE);
			register("timestamp", FineMode.FINE);
		}
	}

	@FineTarget(net.minecraft.entity.ai.EntityAIOwnerHurtTarget.class)
	public static class EntityAIOwnerHurtTargetTypeAdapter extends FineTypeAdapter {

		public EntityAIOwnerHurtTargetTypeAdapter() {
			register("tameable", FineMode.FINE);
			register("attacker", FineMode.FINE);
			register("timestamp", FineMode.FINE);
		}
	}

}
