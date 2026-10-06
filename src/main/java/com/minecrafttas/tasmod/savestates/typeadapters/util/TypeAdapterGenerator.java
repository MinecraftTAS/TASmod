package com.minecrafttas.tasmod.savestates.typeadapters.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.minecrafttas.mctcommon.json.FineTypeAdapterGenerator;

import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;

public class TypeAdapterGenerator {

	public static void main(String[] args) {
		FineTypeAdapterGenerator fgenerator = new FineTypeAdapterGenerator("PathNavigateTypeAdapters", "com.minecrafttas.tasmod.savestates.typeadapters");
		fgenerator.addClass(PathNavigateGround.class, PathNavigateClimber.class, PathNavigateFlying.class, PathNavigateSwimmer.class);
		try {
			Files.write(Paths.get("src/main/java/com/minecrafttas/tasmod/savestates/typeadapters/PathNavigateTypeAdapters.java"), fgenerator.generateMulti(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
