package api.immibis.xpfluid.v1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * Main entry point to the XP Fluid API.
 * 
 * DO NOT MODIFY THIS FILE. EVER. Except for comments.
 */
public final class XPFluidAPI_v1 {
	private XPFluidAPI_v1() {throw new RuntimeException();}
	
	private static Map<Fluid, XPFluidAPIProvider_v1> providers = new IdentityHashMap<>();
	
	private static XPFluidAPIProvider_v1 preferredProvider = null;
	
	static {
		FMLCommonHandler.instance().bus().register(new Object() {
			@SubscribeEvent
			public void resetDefaultFluid(FMLServerAboutToStartEvent evt) {
				preferredProvider = null;
			}
		});
	}
	
	/**
	 * Call this during init to register your provider.
	 * Calling it in preinit is discouraged unless you need to override another mod's provider
	 * (registered in init) for some reason.
	 */
	public static void addProvider(final XPFluidAPIProvider_v1 provider) {
		if(provider == null)
			throw new IllegalArgumentException("Provider is null");
		
		if(Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Providers must be registered in init stage or earlier");
		
		if(providers.containsKey(provider.getFluid()))
			throw new IllegalStateException("Fluid already registered: "+provider.getFluid()+" ("+provider.getFluid().getName()+")");
		
		providers.put(provider.getFluid(), provider);
	}
	
	/**
	 * Returns the provider that has been selected as the user's or modpack builder's favourite
	 * type of XP fluid. This can be configured by setting preferredXPFluid in forge.cfg to the
	 * name of a registered fluid. 
	 */
	public static XPFluidAPIProvider_v1 getPreferredProvider() {
		if(preferredProvider == null) {
			if(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
				throw new IllegalStateException("Preferred provider may be retrieved after init stage");
			if(providers.size() == 0) {
				return null;
			}
			preferredProvider = selectPreferredProvider();
			if(preferredProvider == null)
				throw new AssertionError();
			MinecraftForge.EVENT_BUS.post(new PreferredProviderUpdateEvent());
		}
		return preferredProvider;
	}
	
	private static XPFluidAPIProvider_v1 selectPreferredProvider() {
		Configuration config = ForgeModContainer.getConfig();
		Property p = config.get(Configuration.CATEGORY_GENERAL, "preferredXPFluid", "randomize");
		String value = p.getString();
		
		Fluid f = FluidRegistry.getFluid(value);
		if(f == null || !providers.containsKey(f))
			value = "randomize";
		
		if(value.equals("randomize")) {
			Set<String> fluidNames = new HashSet<>();
			for(Fluid f2 : providers.keySet())
				if(f2 == FluidRegistry.getFluid(f2.getName()))
					fluidNames.add(f2.getName());
			
			List<String> fluidNameList = new ArrayList<>(fluidNames);
			value = fluidNameList.get(new Random().nextInt(fluidNameList.size()));
			
			f = FluidRegistry.getFluid(value);
			
			p.set(value);
			if(config.hasChanged())
				config.save();
		}
		
		return providers.get(f);
	}
	
	public static Fluid[] getXPFluids() {
		if(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Fluid list may be retrieved after init stage");
		return providers.keySet().toArray(new Fluid[0]);
	}
	
	public static XPFluidAPIProvider_v1 getProvider(Fluid f) {
		return providers.get(f);
	}
	
	/**
	 * Returns true if the given fluid stack is a recognized XP fluid.
	 */
	public static boolean isXPFluid(FluidStack fluid) {
		XPFluidAPIProvider_v1 provider = providers.get(fluid.getFluid());
		if(provider != null) {
			return provider.isXPFluid(fluid);
		}
		return false;
	}

	public static boolean isXPFluid(Fluid fluid) {
		return providers.containsKey(fluid);
	}
	
	/**
	 * Fired on the Forge event bus after the preferred provider is selected or read from configuration.
	 */
	public static class PreferredProviderUpdateEvent extends Event {}
}
