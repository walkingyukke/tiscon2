package net.unit8.sigcolle.controller;

import javax.inject.Inject;

import enkan.component.doma2.DomaProvider;
import enkan.data.HttpResponse;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.dao.CampaignDao;
import net.unit8.sigcolle.dao.SignatureDao;
import net.unit8.sigcolle.model.Campaign;

import java.util.ArrayList;
import java.util.List;

/**
 * @author takahashi
 */
public class IndexController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    public HttpResponse index() {

        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        List<Campaign> list = campaignDao.selectAll();
        List<Campaign> list2 = new ArrayList<>();

        int signatureCount=0;
        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);

        for(int i=0;i<list.size();i++) {
            Campaign model = list.get(i);
            signatureCount = signatureDao.countByCampaignId(model.getCampaignId());
            model.setSignatureCount(signatureCount);
            list2.add(model);
        }
        return templateEngine.render("index", "campaigns", list);

    }
}
