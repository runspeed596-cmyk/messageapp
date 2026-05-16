const fs = require('fs');
const path = 'src/pages/OfficialChannelsGroups.tsx';
let content = fs.readFileSync(path, 'utf8');

// Insert renderMultiSelect function before the return statement of OfficialChannelsGroups component
const functionDefinition = `
    const renderMultiSelect = (
        label: string,
        options: { value: string, label: string }[],
        value: string | undefined,
        onChange: (val: string | undefined) => void,
        disabled = false,
        disabledTooltip?: string
    ) => {
        const selectedList = value ? value.split(',').filter(Boolean) : [];
        
        return (
            <div className="space-y-1">
                <label className="text-xs font-bold text-slate-500">{label}</label>
                {selectedList.length > 0 && (
                    <div className="flex flex-wrap gap-1 mb-2">
                        {selectedList.map(item => {
                            const opt = options.find(o => o.value === item);
                            return (
                                <span key={item} className="bg-indigo-500/20 text-indigo-300 text-[10px] px-2 py-1 rounded-md flex items-center gap-1">
                                    {opt ? opt.label : item}
                                    <button type="button" onClick={() => {
                                        const newList = selectedList.filter(i => i !== item);
                                        onChange(newList.length > 0 ? newList.join(',') : undefined);
                                    }} className="hover:text-white"><X size={10} /></button>
                                </span>
                            );
                        })}
                    </div>
                )}
                <select
                    value=""
                    onChange={(e) => {
                        const val = e.target.value;
                        if (!val) return;
                        if (!selectedList.includes(val)) {
                            onChange([...selectedList, val].join(','));
                        }
                    }}
                    className="w-full glass bg-white/5 border-white/5 p-3 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none text-sm"
                    disabled={disabled}
                    title={disabled ? disabledTooltip : ''}
                >
                    <option value="" className="bg-slate-800">انتخاب کنید...</option>
                    {options.filter(o => !selectedList.includes(o.value)).map(o => (
                        <option key={o.value} value={o.value} className="bg-slate-800">{o.label}</option>
                    ))}
                </select>
            </div>
        );
    };

    return (
`;

content = content.replace('    return (\n        <div className="space-y-8 animate-in fade-in duration-500">', functionDefinition + '        <div className="space-y-8 animate-in fade-in duration-500">');

// We need to replace the 6 selectors for both Channel and Group forms!
// Since the structure is quite regular, I can use regex to replace each block with a call to renderMultiSelect.
// However, the options are dynamic.
// Province: options={provinces.map(p => ({value: p, label: p}))}
// City: options={cities.map(c => ({value: c, label: c}))}
// University: options={universities.map(u => ({value: u.name, label: u.name}))}
// Ministry: options: [{value: "وزارت علوم", label: "وزارت علوم"}, ...]
// Field of Study: options={fieldsOfStudy.map(f => ({value: f.name, label: f.name}))}
// Education Level: options={educationLevels.map(el => ({value: el.name, label: el.name}))}

// Instead of regex, I'll just replace the entire <div className="grid grid-cols-1 md:grid-cols-2 gap-4"> block for channel and group.

function generateTargetingFields(formName) {
    const isChannel = formName === 'channelForm';
    const formVar = isChannel ? 'channelForm' : 'groupForm';
    const setFormFn = isChannel ? 'setChannelForm' : 'setGroupForm';

    return `<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {renderMultiSelect(
                                            "استان",
                                            provinces.map(p => ({ value: p, label: p })),
                                            ${formVar}.targetProvince,
                                            (val) => {
                                                ${setFormFn}({ ...${formVar}, targetProvince: val, targetCity: undefined });
                                                // Load cities for the LAST selected province or all selected provinces?
                                                // Since cities API takes a single string, we'll just load cities for the last selected province or clear it.
                                                if (val) {
                                                    const arr = val.split(',');
                                                    loadCities(arr[arr.length - 1]);
                                                } else {
                                                    loadCities("");
                                                }
                                            }
                                        )}
                                        {renderMultiSelect(
                                            "شهر",
                                            cities.map(c => ({ value: c, label: c })),
                                            ${formVar}.targetCity,
                                            (val) => ${setFormFn}({ ...${formVar}, targetCity: val }),
                                            !${formVar}.targetProvince,
                                            "ابتدا استان را انتخاب کنید"
                                        )}
                                        {renderMultiSelect(
                                            "دانشگاه",
                                            universities.map(u => ({ value: u.name, label: u.name })),
                                            ${formVar}.targetUniversity,
                                            (val) => ${setFormFn}({ ...${formVar}, targetUniversity: val })
                                        )}
                                        {renderMultiSelect(
                                            "وزارت مربوطه",
                                            [
                                                "وزارت علوم", "وزارت بهداشت", "پیام نور", "دانشگاه آزاد", "فنی حرفه ای",
                                                "منابع طبیعی", "علمی کاربردی", "غیرانتفاعی", "ملی مهارت", "علوم قرآن و معارف",
                                                "هنر", "موسسه آموزش عالی", "فرهنگیان", "علوم پزشکی"
                                            ].map(m => ({ value: m, label: m })),
                                            ${formVar}.targetMinistry,
                                            (val) => ${setFormFn}({ ...${formVar}, targetMinistry: val })
                                        )}
                                        {renderMultiSelect(
                                            "رشته تحصیلی",
                                            fieldsOfStudy.map(f => ({ value: f.name, label: f.name })),
                                            ${formVar}.targetFieldOfStudy,
                                            (val) => ${setFormFn}({ ...${formVar}, targetFieldOfStudy: val })
                                        )}
                                        {renderMultiSelect(
                                            "مقطع تحصیلی",
                                            educationLevels.map(el => ({ value: el.name, label: el.name })),
                                            ${formVar}.targetEducationLevel,
                                            (val) => ${setFormFn}({ ...${formVar}, targetEducationLevel: val })
                                        )}
                                    </div>`;
}

// Find the block for Channel
const channelRegex = /<div className="grid grid-cols-1 md:grid-cols-2 gap-4">[\s\S]*?{renderMultiSelect\("مقطع تحصیلی"[\s\S]*?<\/div>|<\/div>\s*<\/div>\s*<\/div>\s*<div className="flex gap-2 justify-end pt-4">/g;

// Actually it's easier to use a substring replace
const gridStart = '<div className="grid grid-cols-1 md:grid-cols-2 gap-4">';
const gridEnd = '</div>\n                                    </div>\n                                </div>\n                            )}\n                            <div className="flex gap-2 justify-end pt-4">';

let sections = content.split('فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود\n                                    </p>\n                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">');

if (sections.length === 3) {
    // Channel is sections[1], Group is sections[2]
    
    // Process Channel
    let channelPart = sections[1];
    let channelEndIndex = channelPart.indexOf('</div>\n                                </div>\n                            )}\n                            <div className="flex gap-2 justify-end pt-4">');
    let newChannelPart = '\n                                    ' + generateTargetingFields('channelForm') + '\n                                ' + channelPart.substring(channelEndIndex);
    
    // Process Group
    let groupPart = sections[2];
    let groupEndIndex = groupPart.indexOf('</div>\n                                </div>\n                            )}\n                            <div className="flex gap-2 justify-end pt-4">');
    let newGroupPart = '\n                                    ' + generateTargetingFields('groupForm') + '\n                                ' + groupPart.substring(groupEndIndex);
    
    content = sections[0] + 
              'فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود\n                                    </p>' + 
              newChannelPart + 
              'فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود\n                                    </p>' + 
              newGroupPart;
}

fs.writeFileSync(path, content, 'utf8');
console.log('MultiSelect UI injected!');
