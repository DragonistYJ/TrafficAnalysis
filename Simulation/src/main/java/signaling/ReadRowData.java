package signaling;

import bean.Signaling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReadRowData {
    public List<Signaling> read(String path) {
        List<Signaling> signalings = new ArrayList<>();
        BufferedReader bf = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(path)));
        String line = null;
        try {
            bf.readLine();
            while ((line = bf.readLine()) != null) {
                String[] split = line.split(",");
                // 去除空间信息残缺的记录条目
                if (split[1].equals("") || split[2].equals("") || split[3].equals("") || split[4].equals("")) continue;
                // 去除imsi中，包含特殊字符的数据条目
                boolean flag = false;
                for (int i = 0; i < split[1].length(); i++) {
                    if (!Character.isDigit(split[1].charAt(i))) {
                        flag = true;
                        break;
                    }
                }
                if (flag) continue;
                // 去除非10月3日数据
                long timestamp = Long.parseLong(split[0]);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                if (calendar.get(Calendar.DAY_OF_MONTH) != 3) continue;

                signalings.add(new Signaling(
                        timestamp,
                        split[1],
                        split[2],
                        split[3],
                        split[4]
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return signalings;
    }
}
