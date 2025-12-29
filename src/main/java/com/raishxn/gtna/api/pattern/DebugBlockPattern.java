package com.raishxn.gtna.api.pattern;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DebugBlockPattern {
    public RelativeDirection[] structureDir;
    public String[][] pattern;
    public int[][] aisleRepetitions;
    public Map<Character, Set<String>> symbolMap;
    public Map<Block, Character> legend;

    public DebugBlockPattern() {
        this.legend = new HashMap();
        this.symbolMap = new HashMap();
        this.structureDir = new RelativeDirection[]{RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT};
    }

    public DebugBlockPattern(Level world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this();
        this.pattern = new String[1 + maxX - minX][1 + maxY - minY];
        this.aisleRepetitions = new int[this.pattern.length][2];

        for(int[] aisleRepetition : this.aisleRepetitions) {
            aisleRepetition[0] = 1;
            aisleRepetition[1] = 1;
        }

        this.legend.put(Blocks.AIR, ' ');
        char c = 'A';

        for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
                StringBuilder builder = new StringBuilder();

                for(int z = minZ; z <= maxZ; ++z) {
                    Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (!this.legend.containsKey(block)) {
                        this.legend.put(block, c);
                        String name = String.valueOf(c);
                        ((Set)this.symbolMap.computeIfAbsent(c, (key) -> new HashSet())).add(name);
                        ++c;
                    }

                    builder.append(this.legend.get(block));
                }

                this.pattern[x - minX][y - minY] = builder.toString();
            }
        }

        RelativeDirection[] dirs = getDir(Direction.NORTH);
        this.changeDir(dirs[0], dirs[1], dirs[2]);
    }

    public static RelativeDirection[] getDir(Direction facing) {
        if (facing == Direction.WEST) {
            return new RelativeDirection[]{RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK};
        } else if (facing == Direction.EAST) {
            return new RelativeDirection[]{RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.FRONT};
        } else if (facing == Direction.NORTH) {
            return new RelativeDirection[]{RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT};
        } else if (facing == Direction.SOUTH) {
            return new RelativeDirection[]{RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT};
        } else {
            return facing == Direction.DOWN ? new RelativeDirection[]{RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP} : new RelativeDirection[]{RelativeDirection.LEFT, RelativeDirection.FRONT, RelativeDirection.UP};
        }
    }

    public void changeDir(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        if (!charDir.isSameAxis(stringDir) && !stringDir.isSameAxis(aisleDir) && !aisleDir.isSameAxis(charDir)) {
            char[][][] newPattern = new char[this.structureDir[0].isSameAxis(aisleDir) ? this.pattern[0][0].length() : (this.structureDir[1].isSameAxis(aisleDir) ? this.pattern[0].length : this.pattern.length)][this.structureDir[0].isSameAxis(stringDir) ? this.pattern[0][0].length() : (this.structureDir[1].isSameAxis(stringDir) ? this.pattern[0].length : this.pattern.length)][this.structureDir[0].isSameAxis(charDir) ? this.pattern[0][0].length() : (this.structureDir[1].isSameAxis(charDir) ? this.pattern[0].length : this.pattern.length)];

            for(int i = 0; i < this.pattern.length; ++i) {
                for(int j = 0; j < this.pattern[0].length; ++j) {
                    for(int k = 0; k < this.pattern[0][0].length(); ++k) {
                        char c = this.pattern[i][j].charAt(k);
                        int x = 0;
                        int y = 0;
                        int z = 0;
                        if (this.structureDir[2].isSameAxis(aisleDir)) {
                            if (this.structureDir[2] == aisleDir) {
                                x = i;
                            } else {
                                x = this.pattern.length - i - 1;
                            }
                        } else if (this.structureDir[2].isSameAxis(stringDir)) {
                            if (this.structureDir[2] == stringDir) {
                                y = i;
                            } else {
                                y = this.pattern.length - i - 1;
                            }
                        } else if (this.structureDir[2].isSameAxis(charDir)) {
                            if (this.structureDir[2] == charDir) {
                                z = i;
                            } else {
                                z = this.pattern.length - i - 1;
                            }
                        }

                        if (this.structureDir[1].isSameAxis(aisleDir)) {
                            if (this.structureDir[1] == aisleDir) {
                                x = j;
                            } else {
                                x = this.pattern[0].length - j - 1;
                            }
                        } else if (this.structureDir[1].isSameAxis(stringDir)) {
                            if (this.structureDir[1] == stringDir) {
                                y = j;
                            } else {
                                y = this.pattern[0].length - j - 1;
                            }
                        } else if (this.structureDir[1].isSameAxis(charDir)) {
                            if (this.structureDir[1] == charDir) {
                                z = j;
                            } else {
                                z = this.pattern[0].length - j - 1;
                            }
                        }

                        if (this.structureDir[0].isSameAxis(aisleDir)) {
                            if (this.structureDir[0] == aisleDir) {
                                x = k;
                            } else {
                                x = this.pattern[0][0].length() - k - 1;
                            }
                        } else if (this.structureDir[0].isSameAxis(stringDir)) {
                            if (this.structureDir[0] == stringDir) {
                                y = k;
                            } else {
                                y = this.pattern[0][0].length() - k - 1;
                            }
                        } else if (this.structureDir[0].isSameAxis(charDir)) {
                            if (this.structureDir[0] == charDir) {
                                z = k;
                            } else {
                                z = this.pattern[0][0].length() - k - 1;
                            }
                        }

                        newPattern[x][y][z] = c;
                    }
                }
            }

            this.pattern = new String[newPattern.length][newPattern[0].length];

            for(int i = 0; i < this.pattern.length; ++i) {
                for(int j = 0; j < this.pattern[0].length; ++j) {
                    StringBuilder builder = new StringBuilder();

                    for(char c : newPattern[i][j]) {
                        builder.append(c);
                    }

                    this.pattern[i][j] = builder.toString();
                }
            }

            this.aisleRepetitions = new int[this.pattern.length][2];

            for(int[] aisleRepetition : this.aisleRepetitions) {
                aisleRepetition[0] = 1;
                aisleRepetition[1] = 1;
            }

            this.structureDir = new RelativeDirection[]{charDir, stringDir, aisleDir};
        }
    }

    public DebugBlockPattern copy() {
        DebugBlockPattern newPattern = new DebugBlockPattern();
        System.arraycopy(this.structureDir, 0, newPattern.structureDir, 0, this.structureDir.length);
        newPattern.pattern = new String[this.pattern.length][this.pattern[0].length];

        for(int i = 0; i < this.pattern.length; ++i) {
            System.arraycopy(this.pattern[i], 0, newPattern.pattern[i], 0, this.pattern[i].length);
        }

        newPattern.aisleRepetitions = new int[this.aisleRepetitions.length][2];

        for(int i = 0; i < this.aisleRepetitions.length; ++i) {
            System.arraycopy(this.aisleRepetitions[i], 0, newPattern.aisleRepetitions[i], 0, this.aisleRepetitions[i].length);
        }

        this.symbolMap.forEach((k, v) -> newPattern.symbolMap.put(k, new HashSet(v)));
        return newPattern;
    }
}
