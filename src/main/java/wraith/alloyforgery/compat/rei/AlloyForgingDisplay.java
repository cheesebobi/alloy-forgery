package wraith.alloyforgery.compat.rei;

import com.google.common.collect.ImmutableMap;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.*;

public class AlloyForgingDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final EntryIngredient output;

    public final int minForgeTier;
    public final int requiredFuel;

    public final Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides;

    public final Optional<Identifier> recipeID;

    private AlloyForgingDisplay(List<EntryIngredient> inputs, EntryIngredient output, int minForgeTier, int requiredFuel, Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides, Optional<Identifier> recipeID) {
        this.inputs = inputs;
        this.output = output;

        this.minForgeTier = minForgeTier;
        this.requiredFuel = requiredFuel;

        this.overrides = overrides;

        this.recipeID = recipeID;
    }

    public static AlloyForgingDisplay of(RecipeEntry<AlloyForgeRecipe> recipeEntry) {
        List<EntryIngredient> convertedInputs = new ArrayList<>();

        var recipe = recipeEntry.value();

        for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientsMap().entrySet()) {
            for (int i = entry.getValue(); i > 0; ) {
                int stackCount = Math.min(i, 64);

                convertedInputs.add(
                        EntryIngredients.ofItemStacks(Arrays.stream(entry.getKey().getMatchingStacks())
                                .map(ItemStack::copy)
                                .peek(stack -> stack.setCount(stackCount))
                                .toList()));

                i -= stackCount;
            }
        }

        return new AlloyForgingDisplay(
                convertedInputs,
                EntryIngredients.of(recipe.getBaseResult()),
                recipe.getMinForgeTier(),
                recipe.getFuelPerTick(),
                recipe.getTierOverrides(),
                recipe.secondaryID().or(() -> Optional.of(recipeEntry.id())));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AlloyForgeryCommonPlugin.ID;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return recipeID;
    }

    public enum Serializer implements DisplaySerializer<AlloyForgingDisplay> {

        INSTANCE;

        @Override
        public NbtCompound save(NbtCompound tag, AlloyForgingDisplay display) {
            // Store the fuel per tick
            tag.putInt("fuel_per_tick", display.requiredFuel);

            // Save the minimum Forge Tier
            tag.putInt("min_forge_tier", display.minForgeTier);

            // Store the recipe inputs
            NbtList inputs = new NbtList();
            inputs.addAll(display.inputs.stream().map(EntryIngredient::saveIngredient).toList());
            tag.put("inputs", inputs);

            // Store the recipe output
            tag.put("output", display.output.saveIngredient());

            NbtList overrides = new NbtList();
            display.overrides.forEach((overrideRange, itemStack) -> {
                NbtCompound overrideTag = new NbtCompound();

                overrideTag.putInt("lower", overrideRange.lowerBound());
                overrideTag.putInt("upper", overrideRange.upperBound());
                overrideTag.put("stack", itemStack.getOrCreateNbt());

                overrides.add(overrideTag);
            });
            tag.put("overrides", overrides);

            display.recipeID.ifPresent(id -> tag.putString("recipeID", id.toString()));

            return tag;
        }

        @Override
        public AlloyForgingDisplay read(NbtCompound tag) {
            // Get the fuel per tick
            int requiredFuel = tag.getInt("fuel_per_tick");

            // Get the minimum Forge Tier
            int minForgeTier = tag.getInt("fuel_per_tick");

            // We get a list of all the recipe inputs
            List<EntryIngredient> input = new ArrayList<>();
            tag.getList("inputs", NbtElement.LIST_TYPE).forEach(nbtElement -> input.add(EntryIngredient.read((NbtList) nbtElement)));

            // We get the single recipe output
            EntryIngredient output = EntryIngredient.read(tag.getList("output", NbtElement.LIST_TYPE));

            // Last thing we grab is the recipes Override Range Values
            ImmutableMap.Builder<AlloyForgeRecipe.OverrideRange, ItemStack> builder = new ImmutableMap.Builder<>();
            tag.getList("overrides", NbtElement.COMPOUND_TYPE).forEach(nbtElement -> {
                NbtCompound overrideTag = (NbtCompound) nbtElement;

                AlloyForgeRecipe.OverrideRange range = new AlloyForgeRecipe.OverrideRange(overrideTag.getInt("lower"), overrideTag.getInt("upper"));
                ItemStack stack = ItemStack.fromNbt(overrideTag.getCompound("stack"));

                builder.put(range, stack);
            });

            var recipeID = tag.contains("recipeID") ? Identifier.tryParse(tag.getString("recipeID")) : null;

            return new AlloyForgingDisplay(input, output, minForgeTier, requiredFuel, builder.build(), Optional.ofNullable(recipeID));
        }
    }
}
