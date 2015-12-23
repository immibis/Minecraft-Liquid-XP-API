package api.immibis.xpfluid.v1;

import java.util.*;

import com.google.common.base.Joiner;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
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
	private static boolean preferredProviderSet = false;
	
	/** This has to be public because Forge. Don't use it. */
	public static class EventHandler {
		@Subscribe
		public void resetDefaultFluid(FMLServerAboutToStartEvent evt) {
			preferredProviderSet = false;
		}
		@SubscribeEvent
		public void resetDefaultFluid(FMLNetworkEvent.ClientConnectedToServerEvent evt) {
			preferredProviderSet = false;
		}
		EventHandler() {}
	}
	
	static {
		EventHandler handler = new EventHandler();
		
		FMLCommonHandler.instance().bus().register(handler);
		
		// FMLServerAboutToStartEvents fire on this bus
		
		LoadController c = ReflectionHelper.getPrivateValue(Loader.class, Loader.instance(), "modController");
		EventBus eb = ReflectionHelper.getPrivateValue(LoadController.class, c, "masterChannel");
		eb.register(handler);
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
	 * 
	 * Note that this may return null!
	 */
	public static XPFluidAPIProvider_v1 getPreferredProvider() {
		if(!preferredProviderSet) {
			if(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
				throw new IllegalStateException("Preferred provider may be retrieved after init stage");
			if(providers.size() == 0) {
				return null;
			}
			preferredProvider = selectPreferredProvider();
			preferredProviderSet = true;
			MinecraftForge.EVENT_BUS.post(new PreferredProviderUpdateEvent());
		}
		return preferredProvider;
	}
	
	private static XPFluidAPIProvider_v1 selectPreferredProvider() {
		Configuration config = ForgeModContainer.getConfig();
		
		Set<String> fluidNames = new HashSet<>();
		for(Fluid f2 : providers.keySet())
			if(f2 == FluidRegistry.getFluid(f2.getName()))
				fluidNames.add(f2.getName());
		
		if(fluidNames.size() == 0)
			return null;
		
		List<String> fluidNameList = new ArrayList<>(fluidNames);
		Collections.sort(fluidNameList);
		
		String comment = "Possible values: "+Joiner.on(", ").join(fluidNameList);
		Property p = config.get(Configuration.CATEGORY_GENERAL, "preferredXPFluid", "randomize", comment);
		String value = p.getString();
		
		Fluid f = FluidRegistry.getFluid(value);
		if(f == null || !providers.containsKey(f))
			value = "randomize";
		
		if(value.equals("randomize")) {
			value = fluidNameList.get(new Random().nextInt(fluidNameList.size()));
			
			f = FluidRegistry.getFluid(value);
			
			p.set(value);
		}
		
		if(config.hasChanged())
			config.save();
		
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

	public static XPFluidAPIProvider_v1 getProvider(FluidStack stack) {
		if(stack == null)
			return null;
		XPFluidAPIProvider_v1 provider = getProvider(stack.getFluid());
		if(provider == null)
			return null;
		if(!provider.isXPFluid(stack))
			return null;
		return provider;
	}
}
