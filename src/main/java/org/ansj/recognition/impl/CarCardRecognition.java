package org.ansj.recognition.impl;

import org.ansj.domain.Nature;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.Recognition;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 车牌号码识别抽取
 *
 * @author sunyang
 */
public class CarCardRecognition implements Recognition {

    // 车牌信息
    private static final Nature CPHM_NATURE = new Nature("cphm");
    private static final Pattern CPHM_PATTERN_ALL = Pattern.compile("([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{1}(([A-HJ-Z]{1}[A-HJ-NP-Z0-9]{5})|([A-HJ-Z]{1}(([DF]{1}[A-HJ-NP-Z0-9]{1}[0-9]{4})|([0-9]{5}[DF]{1})))|([A-HJ-Z]{1}[A-D0-9]{1}[0-9]{3}警)))|([0-9]{6}使)|((([沪粤川云桂鄂陕蒙藏黑辽渝]{1}A)|鲁B|闽D|蒙E|蒙H)[0-9]{4}领)|(WJ[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼·•]{1}[0-9]{4}[TDSHBXJ0-9]{1})|([VKHBSLJNGCE]{1}[A-DJ-PR-TVY]{1}[0-9]{5})");
    private static final HashSet<String> CPHM_START = new HashSet();

    static {
        char[] cpsw = "京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼VKHBSLJNGCE0123456789".toCharArray();
        int cpswnub = cpsw.length;

        for (int index = 0; index < cpswnub; ++index) {
            char c = cpsw[index];
            CPHM_START.add(String.valueOf(c));
        }
        CPHM_START.add("WJ");
    }

    @Override
    public void recognition(Result result) {
        if (!result.getTerms().isEmpty()) {
            String name = "";
            String cphmWord = "";
            List<Term> terms = result.getTerms();
            LinkedList<Term> mergeList = new LinkedList();
            List<Term> list = new LinkedList();

            for (int i = 0; i < terms.size(); ++i) {
                boolean isCphm = false;
                Term termBase = (Term)terms.get(i);
                int cphmTermsLength = 1;
                int matchLength = 0;
                boolean isStartedWithCphm = false;
                String cname = termBase.getName();
                int j = 0;

                if (CPHM_START.contains(cname)) {
                    isStartedWithCphm = true;
                } else {
                    for (int k = Math.min(3, cname.length()); j < k; ++j) {
                        String substr = cname.substring(0, j);
                        if (CPHM_START.contains(substr)) {
                            isStartedWithCphm = true;
                            break;
                        }
                    }
                }

                if (isStartedWithCphm) {
                    for (j = i; j < terms.size() && matchLength < 11; ++j) {
                        Term term = (Term)terms.get(j);
                        name = term.getName().toUpperCase();
                        cphmWord = cphmWord + name;
                        Matcher matcher = CPHM_PATTERN_ALL.matcher(cphmWord);
                        mergeList.add(term);
                        if (matcher.matches()) {
                            isCphm = true;
                            cphmTermsLength += j - i;
                            i = j;
                        }

                        ++matchLength;
                    }
                }

                if (!isCphm) {
                    list.add(termBase);
                } else {
                    Term ft = (Term)mergeList.pollFirst();

                    for (int k = 0; k < cphmTermsLength - 1; ++k) {
                        ft.merageWithBlank((Term)mergeList.get(k));
                    }

                    ft.setNature(CPHM_NATURE);
                    list.add(ft);
                }

                mergeList.clear();
                cphmWord = "";
            }

            result.setTerms(list);
        }
    }


}
