package net.unit8.sigcolle.controller;

import javax.inject.Inject;
import javax.transaction.Transactional;

import enkan.component.doma2.DomaProvider;
import enkan.data.Flash;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.auth.LoginUserPrincipal;
import net.unit8.sigcolle.dao.CampaignDao;
import net.unit8.sigcolle.dao.SignatureDao;
import net.unit8.sigcolle.dao.UserDao;
import net.unit8.sigcolle.form.CampaignCreateForm;
import net.unit8.sigcolle.form.CampaignForm;
import net.unit8.sigcolle.form.SignatureForm;
import net.unit8.sigcolle.model.Campaign;
import net.unit8.sigcolle.model.Signature;
import net.unit8.sigcolle.model.User;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static enkan.util.HttpResponseUtils.redirect;
import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
public class CampaignController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;


    /**
     * キャンペーン詳細画面表示.
     *
     * @param form  URLパラメータ
     * @param flash flash scope session
     * @return HttpResponse
     */
    public HttpResponse index(CampaignForm form, Flash flash) {
        if (form.hasErrors()) {
            HttpResponse response = HttpResponse.of("Invalid");
            response.setStatus(400);
            return response;
        }

        return showCampaign(form.getCampaignIdLong(),
                new SignatureForm(),
                (String) some(flash, Flash::getValue).orElse(null));
    }

    /**
     * 署名の追加処理.
     *
     * @param form 画面入力された署名情報.
     * @return HttpResponse
     */
    @Transactional
    public HttpResponse sign(SignatureForm form) {
        if (form.hasErrors()) {
            return showCampaign(form.getCampaignIdLong(), form, null);
        }
        Signature signature = new Signature();
        signature.setCampaignId(form.getCampaignIdLong());
        signature.setName(form.getName());
        signature.setSignatureComment(form.getSignatureComment());

        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);
        signatureDao.insert(signature);

        HttpResponse response = redirect("/campaign/" + form.getCampaignId(), SEE_OTHER);
        response.setFlash(new Flash<>("ＹＥＳ！！　Ｗｅ　ｃａｎ　！！ 　"));
        return response;
    }

    /**
     * 新規キャンペーン作成画面表示.
     *
     * @return HttpResponse
     */
    public HttpResponse newCampaign() {
        return templateEngine.render("campaign/new", "form", new CampaignCreateForm());
    }

    /**
     * 新規キャンペーンを作成します.
     * ---------------------------------------
     * FIXME このメソッドは作成完了しました*
     * @param form    入力フォーム
     * @param session ログインしているユーザsession
     */
    @Transactional
    public HttpResponse create(CampaignCreateForm form,
                               Session session) {
        if (form.hasErrors()) {
            return templateEngine.render("campaign/new", "form", form);
        }
        LoginUserPrincipal principal = (LoginUserPrincipal) session.get("principal");

        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

        // TODO タイトル, 目標人数を登録する
        Campaign model = new Campaign();
        model.setTitle(form.getTitle());//タイトル追加
        model.setGoal(Long.parseLong(form.getGoal()));//目標人数追加
        model.setStatement(processor.markdownToHtml(form.getStatement()));
        model.setCreateUserId(principal.getUserId());



        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        // TODO Databaseに登録する
        campaignDao.insert(model);//DBに追加

        HttpResponse response = redirect("/campaign/" + model.getCampaignId(), SEE_OTHER);
        response.setFlash(new Flash<>("ＹＥＳ！！　Ｗｅ　ｃａｎ　！！"));//キャンペーン作成
        return response;
    }

    /**
     * ログインユーザの作成したキャンペーン一覧を表示します.
     * ---------------------------------------
     * FIXME このメソッドは作成完了しました.
     *
     * @param session ログインしているユーザsession
     */
    public HttpResponse listCampaigns(Session session) {
        HttpResponse response = redirect("/", SEE_OTHER);
        response.setSession(session);
        return response;
    }

    private HttpResponse showCampaign(Long campaignId,
                                      SignatureForm form,
                                      String message) {
        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        Campaign campaign = campaignDao.selectById(campaignId);
        UserDao userDao = domaProvider.getDao(UserDao.class);
        User user = userDao.selectByUserId(campaign.getCreateUserId());
        int signatureCount = 0;
        int rest =0;
        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);
        signatureCount = signatureDao.countByCampaignId(campaignId);
        if((campaign.getGoal()-signatureDao.countByCampaignId(campaignId))>0)
        {
            int change = signatureDao.countByCampaignId(campaignId);
            int change1 = new Integer(campaign.getGoal().toString());
            rest = change1 - change;
        }
        if((campaign.getGoal()-signatureDao.countByCampaignId(campaignId))<=0)
        {
             rest = 0;
        }



        return templateEngine.render("campaign/index",
                "campaign", campaign,
                "user", user,
                "signatureCount", signatureCount,
                "rest", rest,
                "signature", form,
                "message", message
        );
    }
}
