package net.sacredlabyrinth.Phaed.PreciousStones;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author cc_madelg
 */
@Entity()
@CacheStrategy
@Table(name = "cloaked_blocks")
public class CloakEntry implements Serializable
{
    private byte dataByte;

    @OneToMany(mappedBy = "cloakEntry", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @NotNull
    private List<PSItemStack> stacks;

    @Id
    private Long id;
    
    @OneToOne(mappedBy = "cloakEntry")
    private Field field;

    /**
     *
     */
    public CloakEntry()
    {
    }

    /**
     *
     * @param data
     */
    public CloakEntry(byte data)
    {
        this.dataByte = data;
    }

    /**
     *
     * @return
     */
    public byte getDataByte()
    {
        return dataByte;
    }

    /**
     *
     * @param data
     */
    public void setDataByte(byte data)
    {
        this.dataByte = data;
    }

    /**
     *
     * @param stacks
     */
    public void setStacks(List<PSItemStack> stacks)
    {
        this.stacks = stacks;
    }

    /**
     *
     * @return
     */
    public List<PSItemStack> getStacks()
    {
        if (stacks == null)
        {
            return new ArrayList<PSItemStack>();
        }

        return stacks;
    }

    /**
     *
     * @param stacks
     */
    public void importStacks(ItemStack[] stacks)
    {
        this.setStacks(new ArrayList<PSItemStack>());

        for (ItemStack stack : stacks)
        {
            if (stack == null)
            {
                this.getStacks().add(null);
            }
            else
            {
                this.getStacks().add(new PSItemStack(stack));
            }
        }
    }

    /**
     *
     * @return
     */
    public ItemStack[] exportStacks()
    {
        ItemStack[] out = new ItemStack[getStacks().size()];

        for(int i = 0; i < getStacks().size(); i++)
        {
            if(getStacks().get(i) == null)
            {
                out[i] = null;
            }
            else
            {
                out[i] = getStacks().get(i).toItemStack();
            }
        }

        return out;
    }

    /**
     *
     * @return
     */
    public Long getId()
    {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(Long id)
    {
        this.id = id;
    }
}
