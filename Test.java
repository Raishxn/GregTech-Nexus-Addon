import java.lang.reflect.Field;
import com.gregtechceu.gtceu.common.data.GTItems;

public class Test {
    public static void main(String[] args) throws Exception {
        for(Field f : GTItems.class.getFields()) {
            System.out.println(f.getName());
        }
    }
}
