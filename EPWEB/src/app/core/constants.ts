import { environment } from './../../environments/environment';


export const CONSTANTS = {
    SPECIALCHAR_REGEX: /^[0-9a-zA-Z]+$/,
    PHONENUMBER_REGEX: /^\(?(\d{3})\)?(\d{3})[-]?(\d{4})|([ ]?Ext[:]?[ ](\d{5}))$/,
    EMAIL_REGEX: /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/,
    ZIP_CODE_REGEX: /^[0-9a-zA-Z\- ]*$/,
    FAX_REGEX: /^\(?(\d{3})\)?(\d{3})[-]?(\d{4})$/,
    REGISTRATION_PHONENUMBER_REGEX: /^\(?([0-9]{3})[\(\)]{0,2}?[-. ]?([0-9]{3})[-. ]?([0-9]{4})?[-. ]?\s?( Ext: \d{1,5})?$/,
    REGISTRATION_FAX_REGEX: /^\(?([0-9]{3})[\(\)]{0,2}?[-. ]?([0-9]{3})[-. ]?([0-9]{4})?[-. ]?\s?( Ext: \d{1,5})?$/,
    SECURITY_OBJ: "securityObj",
    ERR_TITLE: "Error",
    SnotifyToastNotificationConfig: {
        timeout: 3000
    },
    DEFAULT_TOTALRECORD: 10,
    PDF_TYPE: 'application/pdf',
    APP_NAME : "EPWEB",
};

export const MODULE_PATH = {
    AUTH:               CONSTANTS .APP_NAME + "/auth",
    USER:               CONSTANTS .APP_NAME + "/user",
    MANAGE_USERS:       CONSTANTS .APP_NAME + "/manageUsers",
    NOT_FOUND:          CONSTANTS .APP_NAME + "/notfound",
    PROFILE:            CONSTANTS .APP_NAME + "/profile",
    MCLOOKUP:           CONSTANTS .APP_NAME + "/mclookup",
    NOTIFICATIONS:      CONSTANTS .APP_NAME + "/notifications",
    CERTIFICATE:        CONSTANTS .APP_NAME + "/certificates",
    ACORD:              CONSTANTS .APP_NAME + "/acord",
};

export const APP_URI = {
    LOGIN: "login",
    FORGOT_PASSWORD: "forgotPassword",
    RESET_PASSWORD: "resetpassword",
    REGISTER: "register",
    DASHBOARD: "dashboard",
    COMPANY_PROFILE: "companyprofile",
    SECONDARY_USERS: "secondaryUsers",
    ACCOUNTINFO: "accountinfo",
    MC_SELECTION: "mcselection",
    NOTIFICATIONS: "notifications",
    CERTIFICATE_SAVED: "savedCertificates",
    CERTIFICATE_SUBMITTED: "submittedCertificates",
    CERTIFICATE_PENDING: "pendingCertificates",
    CERTIFICATE_ACTIVE: "activeCertificates",
    CERTIFICATE_EXPIRED: "expiredPolicies",
    CERTIFICATE_NAME_CHANGE: "nameChangePendingCertificates",
    IN_PLACE_POLICIES: "inPlacePolicies",
    IN_PLACE_POLICIES_BY_MC: "inPlacePoliciesByMC",
    CERTIFICATE_TABS: "showcertificate",

    /* NEWLY ADDED AFTER MERGING CODE FROM POC WS*/
    POLICY_SELECTION: "policyselection",
    ACORD_PAGE: "acordpage",
    UPDATE_EP: "epselection",
    SELECTED_ACORD_PAGE: "selectedacordpage",
    ACORD_CONFIRMATION_PAGE: "acordconfirmationpage",
    VIEW_INPLACE_POLICY: "viewinplacepolicy",
};

export const URL_PARAM = {
    MC_ACCOUNT_NUMBER: '/:mcAccountNumber',
    CERTIFICATE_STATUS_INDEX: '/:status'
}

export const NAVIGATE_URI = {
    LOGIN: "/" + MODULE_PATH.AUTH + "/" + APP_URI.LOGIN,
    DASHBOARD: "/" + MODULE_PATH.USER + "/" + APP_URI.DASHBOARD,
    COMPANY_PROFILE: "/" + MODULE_PATH.USER + "/" + APP_URI.COMPANY_PROFILE,
    MC_LOOK_UP: "/" + MODULE_PATH.MCLOOKUP + "/" + APP_URI.MC_SELECTION,
    NOTIFICATIONS: "/" + MODULE_PATH.NOTIFICATIONS + "/" + APP_URI.NOTIFICATIONS,
    CERTIFICATE_SAVED: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_SAVED,
    CERTIFICATE_SUBMITTED: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_SUBMITTED,
    CERTIFICATE_PENDING: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_PENDING,
    CERTIFICATE_ACTIVE: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_ACTIVE,
    CERTIFICATE_EXPIRED: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_EXPIRED,
    CERTIFICATE_NAME_CHANGE: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_NAME_CHANGE,
    IN_PLACE_POLICIES: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.IN_PLACE_POLICIES,
    IN_PLACE_POLICIES_BY_MC: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.IN_PLACE_POLICIES_BY_MC,
    CERTIFICATE_TABS: "/" + MODULE_PATH.CERTIFICATE + "/" + APP_URI.CERTIFICATE_TABS,

     /* NEWLY ADDED AFTER MERGING CODE FROM POC WS*/
     POLICY_SELECTION: "/" + MODULE_PATH.ACORD + "/" + APP_URI.POLICY_SELECTION,
     ACORD_PAGE: "/" + MODULE_PATH.ACORD + "/" + APP_URI.ACORD_PAGE,
     UPDATE_EP: "/" + MODULE_PATH.ACORD + "/" + APP_URI.UPDATE_EP,
     SELECTED_ACORD_PAGE: "/" + MODULE_PATH.ACORD + "/" + APP_URI.SELECTED_ACORD_PAGE,
     ACORD_CONFIRMATION_PAGE: "/" + MODULE_PATH.ACORD + "/" + APP_URI.ACORD_CONFIRMATION_PAGE,
     VIEW_INPLACE_POLICY: "/" + MODULE_PATH.ACORD + "/" + APP_URI.VIEW_INPLACE_POLICY,
};

export const REDIRECT_URI = {
    LOGIN: MODULE_PATH.AUTH + "/" + APP_URI.LOGIN,
    NOT_FOUND: MODULE_PATH.NOT_FOUND
};

export const CERTIFICATE_TAB_STATUS = [
    'SAVED',
    'SUBMITTED',
    'PENDING',
    'ACTIVE',
    'PAST',
    'NAMECHANGE',
    'IN-PLACE',
]

export const CERTIFICATE_TAB_TITLE = [
    'Saved Certificates for ',
    'Submitted Certificates for ',
    'Pending Certificates for ',
    'Active Certificates for ',
    'Expired/Past Submitted Policies for ',
    'Name Change Pending Certificates for ',
    'In Place Policies for  ',
]

/* -------------------------------------------- REST API ZONE-------------------------- */
export const REST_URI = {
    BASE_URL: environment.BASE_URL,
    AUTH: "auth",
    FORGOT_PASSWORD: "forgotPassword",
    VALIDATE_FORGOTPWD_LINK: "validateForgotPwdLink",
    RESET_PASSWORD: "resetPassword",
    REGISTER: "insuranceAgent/register",
    COMPANYINFO: "insuranceAgent/accountInfo/",
    CHANGEPASSWORD: "changePassword",
    GET_ACCOUNT_SETUP: "insuranceAgent/accountInfo/setup",
    GET_ZIPCODE: "resources/findZip",
    DASHBOARD: "iaDashboard",
    MOTORCARRIERS: "iaMotorCarriers",
    DOWNLOADPDF: "insuranceAgent/downloadRegisteredForm/",
    POLICIES_BY_TIMERANGE: "policiesByTimeRange/",
    SETUP_NOTIFICATION: "setupNotifications",
    NOTIFICATIONS: "notifications",
    NOTIFICATIONS_DOWNLOAD_PDF : "notifications/downloadPdf",
    CERTIFICATE_COUNTS: "insuranceAgent/certificatesCounts",
    SHOW_CERTIFICATES: "insuranceAgent/showCertis",
    GET_CERTIFICATE_LIST: "currentUIIAInsureds",
    ADD_NEW_MC: "addNewMC",
    INPLACE_POLICIES : "inplacePolicies",
    SAVE_MC: "saveMC",

     /* NEWLY ADDED AFTER MERGING CODE FROM POC WS*/	
     EQUIPMENT_PROVIDERS_LIST: 'equipmentProviders',	
};

