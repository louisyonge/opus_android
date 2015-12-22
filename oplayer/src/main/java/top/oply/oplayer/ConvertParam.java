package top.oply.oplayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by young on 2015/8/14.
 */
public class ConvertParam implements Serializable {
    public static final long serialVersionUID=1234567890987654322L;
    //
    private Map<Integer,String> mTypes = new HashMap<Integer, String>(8);
    private Map<Integer,String[]> mData = new HashMap<Integer, String[]>(16);
    private Map<Integer, Integer> mChoice = new HashMap<Integer, Integer>(8);


    public ConvertParam() {
    }

    public void add(int id,String parameter ,String[] valueRange) {
        Integer key = new Integer(id);
        mTypes.put(key, parameter);
        mData.put(key, valueRange);
    }

    public void select(int id, int valueIndex) {
        Integer key = new Integer(id);
        Integer valueIdx = new Integer(valueIndex);
        mChoice.put(key ,valueIdx);
    }

    public String getFinalSelections() {
        String result = new String();
        for(Integer i : mChoice.keySet()) {
            Integer offset = mChoice.get(i);
            result = result + mTypes.get(i) + mData.get(i)[offset.intValue()];
        }

        return result;
    }
    public int getSelectedIndex(int id) {
        Integer i = new Integer(id);
        return mChoice.get(i).intValue();
    }

    public String[] getValues(int id) {
        Integer i = new Integer(id);
        return  mData.get(i);
    }

}