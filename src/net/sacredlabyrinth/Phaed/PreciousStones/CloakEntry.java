package net.sacredlabyrinth.Phaed.PreciousStones;

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
 * @author phaed
 */
@Entity()
@Table(name = "ps_cloaked")
public class CloakEntry implements Serializable
{
    @Id
    private Long id;

    private byte dataByte;

    @OneToMany(mappedBy = "cloakEntry", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PSItemStack> stacks = new ArrayList<PSItemStack>();

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
     * Table identity column
     * @return the id
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the table identity column
     * @param id the id
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * @return the field
     */
    public Field getField()
    {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field)
    {
        this.field = field;
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
        return stacks;
    }

    /*
     *
     */
    /**
     *
     */
    public void clearStacks()
    {
        stacks.clear();
    }

    /**
     *
     * @param nativeStacks
     */
    public void importStacks(ItemStack[] nativeStacks)
    {
        stacks.clear();

        for (ItemStack stack : nativeStacks)
        {
            stacks.add(new PSItemStack(stack));
        }
    }

    /**
     *
     * @return
     */
    public ItemStack[] exportStacks()
    {
        ItemStack[] out = new ItemStack[stacks.size()];

        for(int i = 0; i < stacks.size(); i++)
        {
            if(stacks.get(i).getType() == 0)
            {
                out[i] = null;
            }
            else
            {
                out[i] = stacks.get(i).toItemStack();
            }
        }

        return out;
    }
}
