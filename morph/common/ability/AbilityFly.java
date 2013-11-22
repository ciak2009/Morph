package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;
import morph.common.Morph;
import morph.common.core.SessionState;
import morph.common.morph.MorphInfo;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class AbilityFly extends Ability {

	public boolean slowdownInWater;
	
	public AbilityFly()
	{
		slowdownInWater = true;
	}
	
	public AbilityFly(boolean slowdown)
	{
		slowdownInWater = slowdown;
	}
	
	@Override
	public Ability parse(String[] args)
	{
		try
		{
			slowdownInWater = Boolean.parseBoolean(args[0]);
		}
		catch(Exception e)
		{
		}
		return this;
	}
	
	@Override
	public String getType() 
	{
		return "fly";
	}

	@Override
	public void tick() 
	{
		if(getParent() instanceof EntityPlayer)
		{
			if(!SessionState.allowFlight)
			{
				return;
			}
			EntityPlayer player = (EntityPlayer)getParent();
			if(!player.capabilities.allowFlying)
			{
				player.capabilities.allowFlying = true;
				player.sendPlayerAbilities();
			}
			if(player.capabilities.isFlying && !player.capabilities.isCreativeMode)
			{
				if(!player.worldObj.isRemote)
				{
					double motionX = player.posX - player.lastTickPosX;
					double motionZ = player.posZ - player.lastTickPosZ;
	                int i = Math.round(MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ) * 100.0F);
	
	                if (i > 0)
	                {
	                	if(player.isInWater() && slowdownInWater)
	                	{
	                		player.addExhaustion(0.095F * (float)i * 0.01F);
	                	}
	                	else
	                	{
	                		player.addExhaustion(0.013F * (float)i * 0.01F);
	                	}
	                }
	                else
	                {
	                	player.addExhaustion(0.001F);
	                }
				}
				else if(player.isInWater() && slowdownInWater)
                {
	                MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.username);
	        		
	        		if(info != null)
	        		{
	        			boolean swim = false;
	        			for(Ability ability : info.morphAbilities)
	        			{
	        				if(ability.getType().equalsIgnoreCase("swim"))
	        				{
	        					swim = true;
	        					break;
	        				}
	        			}
	        			if(!swim)
	        			{
	        				player.motionX *= 0.65D;
	        				player.motionZ *= 0.65D;
	        				
	        				player.motionY *= 0.2D;
	        			}
	        		}
                }
			}
		}
		getParent().fallDistance = 0.0F;
		//TODO make "Thing" take note of this so it can fly...
	}

	@Override
	public void kill() 
	{
		if(getParent() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)getParent();
			if(!player.capabilities.isCreativeMode)
			{
				player.capabilities.allowFlying = false;
				if(player.capabilities.isFlying)
				{
					player.capabilities.isFlying = false;
				}
				player.sendPlayerAbilities();
			}
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFly(slowdownInWater);
	}

	@Override
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return SessionState.allowFlight ? iconResource : null;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fly.png");

}