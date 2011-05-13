package net.sacredlabyrinth.Phaed.PreciousStones;

import com.avaje.ebean.annotation.CacheStrategy;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author phaed
 */
@Entity()
@Table(name = "ps_item_stacks")
public class PSItemStack implements Serializable
{
    private int type;
    private int amount = 0;
    private byte dataByte = 0;
    private short durability = 0;

    @Id
    private Long id;

    @ManyToOne
    private CloakEntry cloakEntry;

    /**
     *
     */
    public PSItemStack()
    {
    }

    /**
     *
     * @param stack
     */
    public PSItemStack(ItemStack stack)
    {
        if (stack == null)
        {
            this.type = 0;
            this.amount = 0;
            this.durability = 0;
            this.dataByte = 0;
        }
        else
        {
            this.type = stack.getTypeId();
            this.amount = stack.getAmount();
            this.durability = stack.getDurability();
            this.dataByte = stack.getData() == null ? 0 : stack.getData().getData();
        }
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
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * @return the amount
     */
    public int getAmount()
    {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    /**
     * @return the data
     */
    public byte getDataByte()
    {
        return dataByte;
    }

    /**
     * @param data the data to set
     */
    public void setDataByte(byte data)
    {
        this.dataByte = data;
    }

    /**
     * @return the durability
     */
    public short getDurability()
    {
        return durability;
    }

    /**
     * @param durability the durability to set
     */
    public void setDurability(short durability)
    {
        this.durability = durability;
    }

    /**
     *
     * @return
     */
    public ItemStack toItemStack()
    {
        return new ItemStack(getType(), getAmount(), getDurability(), getDataByte());
    }

    /**
     * @return the cloakEntry
     */
    public CloakEntry getCloakEntry()
    {
        return cloakEntry;
    }

    /**
     * @param cloakEntry the cloakEntry to set
     */
    public void setCloakEntry(CloakEntry cloakEntry)
    {
        this.cloakEntry = cloakEntry;
    }
}
